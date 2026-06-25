# PQC & ML-DSA (1-2단계)

> PLAN.md §1-2 대응 — liboqs-java + ML-DSA 핵심 검증

---

## 이론

### 왜 PQC(Post-Quantum Cryptography)인가

현재 RSA / ECDSA는 **정수 인수분해**와 **이산 로그** 문제의 어려움에 기반합니다.
양자 컴퓨터의 **Shor 알고리즘**은 이 두 문제를 다항 시간에 풀 수 있습니다.
→ 충분히 강력한 양자 컴퓨터가 등장하면 현재 공개키 암호 체계 전체가 깨집니다.

**격자 암호(Lattice Cryptography)** 는 고차원 격자에서 최단 벡터를 찾는 문제(SVP)에 기반합니다.
이 문제는 현재 알려진 양자 알고리즘으로도 지수 시간이 걸립니다.

### NIST PQC 표준 4종

| 표준 | 원래 이름 | 용도 |
|---|---|---|
| ML-KEM (FIPS 203) | CRYSTALS-Kyber | 키 캡슐화 (TLS 키 교환) |
| **ML-DSA (FIPS 204)** | **CRYSTALS-Dilithium** | **전자서명 ← QuSign에서 사용** |
| SLH-DSA (FIPS 205) | SPHINCS+ | 서명 (해시 기반, 보수적) |
| FN-DSA (FIPS 206) | FALCON | 서명 (서명 크기 작음) |

### ML-DSA 키 크기 (ML-DSA-65 기준)

| 항목 | RSA-2048 | ECDSA P-256 | ML-DSA-65 |
|---|---|---|---|
| 공개키 | ~270B | 65B | **1,952B** |
| 개인키 | ~1,200B | 32B | **4,032B** |
| 서명값 | 256B | ~72B | **3,309B** |

→ ML-DSA 서명값이 훨씬 크지만, 서명·검증 속도는 ECDSA와 비슷하거나 더 빠릅니다.

### liboqs-java 구조

```
liboqs (C 라이브러리)
  └── liboqs-java (JNI 바인딩)
       └── BouncyCastle 1.84 (FIPS 204 지원)
```

Spring Boot는 JVM 위에서 동작하므로 네이티브 C 라이브러리를 직접 호출할 수 없습니다.
liboqs-java는 JNI(Java Native Interface)를 통해 C 구현체를 호출합니다.
QuSign은 **BouncyCastle 1.84의 순수 Java 구현**을 사용합니다(네이티브 빌드 불필요).

---

## 핵심 동작 원리

### 서명 흐름

```
[서명자]
1. KeyPair 생성 (publicKey, privateKey)
2. 문서 해시 계산 (SHA3-256)
3. privateKey로 해시 서명 → signature bytes

[검증자]
4. publicKey + 원본 해시 + signature → 검증
5. 검증 성공 → 문서 무결성 + 서명자 신원 확인
```

### 개인키 보호 전략 (QuSign)

사용자 비밀번호로 개인키를 암호화하여 DB에 저장합니다.

```
개인키 → AES-256-GCM 암호화 (PBKDF2로 비밀번호 → 대칭키 유도) → DB 저장
서명 시 → 비밀번호 입력 → 대칭키 복원 → 개인키 복호화 → 서명 → 메모리에서 zeroing
```

---

## 현재 코드에서의 사용 예시

### 키쌍 생성 — `PqcSignatureServiceImpl.kt`
```kotlin
fun generateKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("DILITHIUM", "BC")
    keyPairGenerator.initialize(DilithiumParameterSpec.dilithium3) // ML-DSA-65
    return keyPairGenerator.generateKeyPair()
}
```
`"DILITHIUM"` — BouncyCastle이 ML-DSA-65를 등록하는 알고리즘 이름입니다.
`dilithium3` — 보안 레벨 3 (NIST 기준 128-bit quantum security).

### 서명 — `PqcSignatureServiceImpl.kt`
```kotlin
fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray {
    val signer = Signature.getInstance("DILITHIUM", "BC")
    signer.initSign(privateKey)
    signer.update(data)
    return signer.sign()
}
```

### 검증 — `PqcSignatureServiceImpl.kt`
```kotlin
fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
    val verifier = Signature.getInstance("DILITHIUM", "BC")
    verifier.initVerify(publicKey)
    verifier.update(data)
    return verifier.verify(signature)
}
```

---

## 확인 질문 & 답변

**Q1. ML-DSA가 RSA와 근본적으로 다른 수학적 기반은?**

> RSA는 큰 수의 소인수 분해 어려움에 기반합니다. ML-DSA는 고차원 격자에서 오류 있는 선형 방정식을 푸는 문제(Module Learning With Errors, MLWE)에 기반합니다. 양자 컴퓨터의 Shor 알고리즘은 소인수 분해는 효율적으로 풀지만 MLWE에는 지수 시간이 걸립니다.

**Q2. QuSign에서 개인키를 서버 DB에 저장하는데, 왜 안전한가?**

> 개인키 원문이 아닌 사용자 비밀번호로 AES-256-GCM 암호화된 형태로 저장합니다. 서버가 해킹되더라도 비밀번호를 모르면 개인키를 복호화할 수 없습니다. 서명 시에만 비밀번호를 받아 메모리에서 복호화하고, 서명 완료 후 즉시 zeroing합니다.

**Q3. ML-DSA 서명값 크기(~3.3KB)가 문서에 미치는 영향은?**

> PDF 메타데이터에 Base64 인코딩하여 삽입하므로 약 4.4KB가 추가됩니다. 일반 PDF(수백 KB~수 MB)에 비해 무시할 수 있는 크기입니다. DB와 S3에는 이 크기 그대로 저장됩니다.

**Q4. `dilithium3`을 선택한 이유는?**

> NIST 보안 레벨 3 (128-bit quantum security)로, 보안과 성능의 균형이 가장 좋습니다. `dilithium2`는 보안이 약하고, `dilithium5`는 키/서명 크기가 더 크지만 보안 향상 폭이 크지 않아 현 단계에서는 `dilithium3`(= ML-DSA-65)이 표준 선택입니다.
