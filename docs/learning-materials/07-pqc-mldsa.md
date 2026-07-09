# PQC & ML-DSA (1-2단계)

> PLAN.md §1-2 대응 — liboqs-java + ML-DSA 핵심 검증

---

## 이론

### 왜 PQC인가 — 위협 모델부터 이해하기

#### 현재 공개키 암호의 수학적 기반

RSA와 ECDSA는 각각 다른 수학적 어려움에 기반합니다.

| 알고리즘 | 수학적 기반 | 어려운 이유 |
|---|---|---|
| RSA | 정수 인수분해 (IFP) | N = p × q 가 클 때 p, q를 찾기 어려움 |
| ECDSA | 타원곡선 이산 로그 (ECDLP) | P = k·G 에서 k를 찾기 어려움 |

**현대 컴퓨터**로 2048-bit RSA를 깨려면 우주 나이보다 오래 걸립니다.

> RSA의 자물쇠·열쇠 비유, 인수분해가 어려운 이유(체스판과 밀알 비유), 실제 크랙 기록, 개인키 `d` 계산 과정(확장 유클리드 알고리즘), 소수로 직접 재현한 생성·사용·공격 전 과정 예제는 [06-crypto-fundamentals.md](06-crypto-fundamentals.md)에서 다룹니다. 여기서는 그 기초 위에서 "양자컴퓨터가 등장하면 이 안전성이 왜 무너지는가"부터 이어갑니다.

#### Shor 알고리즘 — 양자 컴퓨터의 위협

1994년 Peter Shor는 양자 컴퓨터로 IFP와 ECDLP를 **다항 시간(polynomial time)**에 푸는 알고리즘을 발표했습니다.

```
고전 컴퓨터: RSA-2048 인수분해 → 수십억 년
양자 컴퓨터(Shor): RSA-2048 인수분해 → 수천 큐비트로 수 시간
```

현재(2025년) 기준으로 암호학적으로 의미 있는 양자 컴퓨터는 없습니다.
그러나 **"지금 훔치고 나중에 해독(Harvest Now, Decrypt Later)"** 공격이 이미 진행 중입니다:

```
공격자가 오늘 암호화된 트래픽을 저장 → 10년 후 양자 컴퓨터로 해독
```

기밀 유지 기간이 10년 이상인 데이터(정부 기밀, 의료 기록, 장기 계약서)는
**지금 당장 PQC로 전환**해야 합니다.

---

### 격자 암호(Lattice Cryptography) — 직관적 이해

#### 격자(Lattice)란

n차원 공간에서 선형적으로 독립적인 기저 벡터들의 정수 선형 결합으로 생성된 이산 점들의 집합입니다.

```
2차원 격자 예시 (기저: v₁=(1,0), v₂=(0,1))
·  ·  ·  ·  ·
·  ·  ·  ·  ·
·  ·  O  ·  ·   ← 모든 정수 좌표점이 격자점
·  ·  ·  ·  ·
·  ·  ·  ·  ·
```

**SVP (Shortest Vector Problem)**: 격자에서 원점에 가장 가까운 비영 벡터를 찾는 문제.
차원이 높아질수록(수백~수천 차원) SVP는 지수적으로 어려워집니다.

#### LWE (Learning With Errors)

격자 암호의 핵심 어려운 문제입니다.

비밀 벡터 **s** 가 있을 때, 다수의 `(aᵢ, bᵢ)` 쌍을 관찰해도 **s** 를 찾기 어렵습니다.

```
bᵢ = aᵢ · s + eᵢ (mod q)
  ↑      ↑      ↑
랜덤벡터  비밀   작은 오류
```

오류 `eᵢ`가 없다면 선형대수로 즉시 풀 수 있지만, 작은 랜덤 오류가 추가되면 풀기 극도로 어려워집니다.
**ML-DSA는 LWE의 모듈 버전(Module-LWE)에 기반합니다.**

#### 왜 격자 문제는 양자 컴퓨터로도 어려운가

Shor 알고리즘은 **푸리에 변환** 기반으로 주기성을 찾습니다.
격자 문제(LWE)는 주기성이 없는 선형 방정식 시스템으로, Shor 알고리즘이 적용되지 않습니다.
현재 가장 빠른 양자 알고리즘으로도 격자 문제는 **지수 시간**이 걸립니다.

---

### NIST PQC 표준화 과정

2016년 NIST가 공모 → 2024년 8월 최종 표준 발표 (FIPS 203/204/205):

```
2016  공모 시작 (69개 제출)
2018  1라운드 (26개 통과)
2020  2라운드 (15개 통과)
2022  3라운드 최종 후보 발표
2024  FIPS 203/204/205/206 최종 표준 발표
```

| 표준 | 원래 이름 | 수학 기반 | 용도 |
|---|---|---|---|
| ML-KEM (FIPS 203) | CRYSTALS-Kyber | Module-LWE | 키 캡슐화 (TLS 키 교환) |
| **ML-DSA (FIPS 204)** | **CRYSTALS-Dilithium** | **Module-LWE + SIS** | **전자서명 ← QuSign** |
| SLH-DSA (FIPS 205) | SPHINCS+ | 해시 함수 | 서명 (보수적·검증 빠름) |
| FN-DSA (FIPS 206) | FALCON | NTRU 격자 | 서명 (서명 작음·구현 복잡) |

---

### ML-DSA 내부 구조 — Fiat-Shamir with Aborts

ML-DSA는 **Fiat-Shamir** 패러다임에 기반하는 격자 서명 방식입니다.

#### 핵심 아이디어

```
서명자는 비밀 정보 s를 가지고 있습니다.
검증자는 공개 정보 A, t = A·s 만 압니다.

서명 시:
1. 랜덤 y 선택 (마스킹 벡터)
2. w = A·y 계산 (커밋먼트)
3. c = H(메시지 || w) 계산 (챌린지 해시)
4. z = y + c·s 계산 (응답)
5. z가 너무 크면 "abort" 후 재시도 ← "with Aborts"의 핵심

검증 시:
A·z - c·t = A·y + A·c·s - c·A·s = A·y = w 확인
```

**"Aborts"가 필요한 이유**: `z = y + c·s` 에서 z가 너무 크면 s의 크기 정보가 노출됩니다. 특정 범위를 벗어난 z는 버리고 재시도하여 s 정보 누출을 방지합니다.

#### ML-DSA 보안 레벨별 파라미터

| 레벨 | 이름 | 양자 보안 | 공개키 | 개인키 | 서명 |
|---|---|---|---|---|---|
| 2 | ML-DSA-44 | 128-bit | 1,312B | 2,528B | 2,420B |
| **3** | **ML-DSA-65** | **192-bit** | **1,952B** | **4,032B** | **3,309B** |
| 5 | ML-DSA-87 | 256-bit | 2,592B | 4,896B | 4,627B |

> QuSign은 **ML-DSA-65 (레벨 3)** — 192-bit 양자 보안으로 향후 10년 이상 안전합니다.

---

### 전자서명 보안 속성

ML-DSA가 달성해야 하는 세 가지 속성:

| 속성 | 의미 | ML-DSA 달성 방법 |
|---|---|---|
| **위조 불가** (EUF-CMA) | 개인키 없이 유효한 서명 생성 불가 | Module-LWE 어려움 |
| **부인 방지** | 서명자가 나중에 서명을 부인할 수 없음 | 개인키와 서명의 수학적 결합 |
| **무결성** | 서명 후 문서 변조 감지 | 서명 대상에 문서 해시 포함 |

---

### liboqs-java vs BouncyCastle 비교

QuSign이 선택한 스택:

```
BouncyCastle 1.84 (순수 Java ML-DSA 구현)
  장점: 네이티브 빌드 불필요, JVM만으로 동작, 이식성 높음
  단점: C 구현보다 약 3~5배 느림 (포트폴리오 단계에서는 무관)

liboqs-java (JNI → C liboqs)
  장점: NIST 참조 구현 기반, C 구현 속도
  단점: 플랫폼별 네이티브 빌드 필요, 운영 복잡도 증가
```

JDK 로드맵 (JEP 497 - JDK 24 Preview): 표준 `java.security` API에 ML-DSA 통합 예정.
BouncyCastle을 쓰면 JDK 표준화 시 인터페이스 변경 없이 교체 가능합니다.

---

### 개인키 보호 전략 (PBKDF2 + AES-256-GCM)

#### PBKDF2 (Password-Based Key Derivation Function 2)

비밀번호(짧고 예측 가능)를 암호화 키(256-bit)로 변환합니다.

```
입력: 비밀번호 + 솔트(random 16B) + 반복횟수(310,000회)
출력: 256-bit 대칭키

반복 310,000회의 의미:
  → 공격자가 패스워드 1개를 테스트하는 데 수십 ms 소요
  → 10억 개 패스워드 시도에 수십 년 필요
```

#### AES-256-GCM (인증된 암호화)

```
평문(개인키) + 키(PBKDF2 결과) + IV(12B nonce)
  → 암호문 + 인증태그(16B)

인증태그가 있으므로:
  → 복호화 시 태그 검증 실패 = 비밀번호 오류 또는 데이터 변조
  → QuSign에서 AEADBadTagException으로 처리
```

```
개인키 원문 → PBKDF2(비밀번호, salt, 310000) → AES-256-GCM → {암호문, IV, salt, 태그}
서명 시    → PBKDF2(입력 비밀번호, salt) → AES-256-GCM 복호화 → 개인키 원문
```

---

## 핵심 동작 원리

### 서명 흐름

```
[회원가입]
1. ML-DSA-65 KeyPair 생성 (publicKey, privateKey)
2. privateKey → PBKDF2 + AES-256-GCM 암호화
3. publicKey + encryptedPrivateKey → DB 저장

[서명 요청 실행]
4. 서명 비밀번호 입력
5. DB에서 encryptedPrivateKey 로드
6. PBKDF2(비밀번호) → AES-256-GCM 복호화 → privateKey
7. SHA3-256(PDF 원본) → documentHash
8. ML-DSA.sign(privateKey, documentHash) → signature bytes
9. Arrays.fill(privateKeyBytes, 0)  ← 즉시 zeroing
10. PDF 메타데이터에 signature + signerId + documentHash 삽입 (공개키는 미삽입 — DB에서 signerId로 조회)
11. 서명된 PDF를 S3에 저장

[무결성 검증]
12. 서명된 PDF에서 signature + signerId + documentHash 추출
13. signerId로 DB에서 publicKey 조회, SHA3-256(원본 PDF) vs 추출한 documentHash 비교
14. ML-DSA.verify(publicKey, documentHash, signature) → true/false
```

---

## 현재 코드에서의 사용 예시

### 키쌍 생성 — `BouncyCastlePqcSignatureService.kt`
```kotlin
override fun generateKeyPair(): KeyPair {
    val kpg = KeyPairGenerator.getInstance("ML-DSA", "BC")
    kpg.initialize(MLDSAParameterSpec.ml_dsa_65)
    return kpg.generateKeyPair()
}
```
`"ML-DSA"` — BouncyCastle이 ML-DSA를 등록하는 알고리즘 이름입니다.
`ml_dsa_65` — 보안 레벨 3 (192-bit quantum security). (구 BouncyCastle 버전의 `"DILITHIUM"`/`DilithiumParameterSpec.dilithium3` 명칭은 FIPS 204 표준화 이후 `"ML-DSA"`/`MLDSAParameterSpec.ml_dsa_65`로 변경되었습니다.)

### 서명 — `BouncyCastlePqcSignatureService.kt`
```kotlin
override fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray {
    val signer = Signature.getInstance("ML-DSA", "BC")
    signer.initSign(privateKey)
    signer.update(message)
    return signer.sign()
}
```

### 검증 — `BouncyCastlePqcSignatureService.kt`
```kotlin
override fun verify(publicKey: PublicKey, message: ByteArray, signature: ByteArray): Boolean {
    val verifier = Signature.getInstance("ML-DSA", "BC")
    verifier.initVerify(publicKey)
    verifier.update(message)
    return verifier.verify(signature)
}
```

### 개인키 암호화/복호화 — `KeyEncryptionService.kt`
```kotlin
data class EncryptedKey(
    val ciphertext: String,
    val salt: String,
    val iv: String,
    val iterations: Int = PBKDF2_ITERATIONS,  // 310_000
)

fun encrypt(plaintext: ByteArray, password: String): EncryptedKey {
    val salt = SecureRandom.getInstanceStrong().generateSeed(SALT_BYTES)   // 16B
    val iv = SecureRandom.getInstanceStrong().generateSeed(IV_BYTES)      // 12B
    val key = deriveKey(password, salt, PBKDF2_ITERATIONS)                // PBKDF2WithHmacSHA256

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

    return EncryptedKey(
        ciphertext = Base64.getEncoder().encodeToString(cipher.doFinal(plaintext)),
        salt = Base64.getEncoder().encodeToString(salt),
        iv = Base64.getEncoder().encodeToString(iv),
    )
}
```
실제 `EncryptedKey`는 암/복호화 결과를 Base64 문자열로 저장하고, PBKDF2 반복 횟수(`iterations`)도 함께 저장합니다 — 나중에 반복 횟수를 올리더라도 기존에 암호화된 키를 그 키가 만들어질 당시의 반복 횟수로 복호화할 수 있도록 하기 위함입니다.

---

## 확인 질문 & 답변

**Q1. ML-DSA가 RSA와 근본적으로 다른 수학적 기반은?**

> RSA는 큰 수의 소인수 분해(N=p×q에서 p,q 복원) 어려움에 기반합니다. ML-DSA는 고차원 격자에서 오류가 포함된 선형 방정식 시스템(Module-LWE)을 푸는 문제에 기반합니다. Shor 알고리즘은 주기 함수의 주기를 양자 푸리에 변환으로 찾는 방식이라 소인수 분해와 이산 로그에 효과적이지만, 격자 문제는 주기성이 없어 적용되지 않습니다.

**Q2. "Harvest Now, Decrypt Later" 공격이 현재 전자서명에 위협이 되는가?**

> 전자서명은 기밀성이 아닌 진위 확인이 목적이므로 직접 위협은 아닙니다. 그러나 **과거 서명의 유효성 부정(Repudiation)**이 문제가 됩니다. 공격자가 현재의 RSA 서명을 저장했다가 양자 컴퓨터로 개인키를 복원하면, 그 키로 임의 문서에 서명하여 과거 서명인 것처럼 위조할 수 있습니다. 전자서명법 기준 10년 보존 의무를 고려하면 지금 ML-DSA로 전환하는 것이 맞습니다.

**Q3. ML-DSA 서명에 "Aborts"가 필요한 이유를 직관적으로 설명하면?**

> 서명 `z = y + c·s` 에서 y가 마스킹 역할을 합니다. 만약 z의 크기가 s의 크기에 비해 너무 작으면 "y가 c·s를 충분히 가리지 못했다"는 신호가 되어 s 정보가 통계적으로 노출됩니다. 안전한 z의 범위를 벗어나면 해당 서명을 버리고 새로운 y로 재시도합니다. 평균 약 4-7번 재시도 후 유효한 서명이 생성됩니다.

**Q4. QuSign에서 개인키를 서버 DB에 저장하는데, 왜 안전한가?**

> 개인키 원문이 아닌 사용자 비밀번호로 AES-256-GCM 암호화된 형태로 저장합니다. 서버가 해킹되더라도 비밀번호를 모르면 개인키를 복호화할 수 없습니다 (AES-256은 양자 컴퓨터로도 안전합니다). 서명 시에만 비밀번호를 받아 메모리에서 복호화하고, 서명 완료 후 즉시 `Arrays.fill(bytes, 0)`으로 zeroing합니다.

**Q5. `ml_dsa_65`(ML-DSA-65)를 선택한 이유는?**

> NIST 보안 레벨 3 (192-bit quantum security)으로 보안과 성능의 균형이 좋습니다. ML-DSA-44(레벨 2, 128-bit)는 현재 충분하나 양자 컴퓨터 발전 속도를 감안하면 마진이 적습니다. ML-DSA-87(레벨 5, 256-bit)는 서명 크기가 40% 더 크지만 실질적 보안 향상이 크지 않아 현 단계에서는 ML-DSA-65가 표준 선택입니다.

**Q6. SHA3-256을 서명 대상으로 쓰는 이유는? ML-DSA가 직접 문서에 서명하면 안 되나?**

> 가능하지만 비효율적입니다. ML-DSA 내부적으로 서명 대상을 해시하지만, 수십 MB PDF를 JNI 경계를 통해 전달하면 메모리 복사 오버헤드가 큽니다. 미리 SHA3-256으로 해시(32B)를 계산하고 해시에 서명하면 속도와 메모리 효율이 향상됩니다. SHA3-256은 SHA-2 계열과 달리 sponge 구조여서 길이 확장 공격에 안전합니다.
