# PQC 하이브리드 TLS (8-4단계)

> PLAN.md §8-4 대응 — ML-KEM + X25519 하이브리드 TLS, ML-DSA 사설 CA

---

## 이론

### TLS 1.3 핸드셰이크 상세

TLS 1.2 대비 TLS 1.3은 왕복 횟수를 줄여(1-RTT) 지연을 낮춥니다.

#### TLS 1.3 핸드셰이크 흐름

```
클라이언트                              서버
    │                                      │
    │──── ClientHello ────────────────────▶│
    │  지원 TLS 버전, 암호 스위트 목록        │
    │  key_share: [X25519, X25519MLKEM768] │  ← 미리 키 공유 제안
    │                                      │
    │ ◀─── ServerHello ───────────────────│
    │  선택된 암호 스위트                    │
    │  key_share: X25519MLKEM768 응답값    │
    │                                      │
    │ ◀─── {EncryptedExtensions} ─────────│
    │ ◀─── {Certificate} ─────────────────│  ← 서버 인증서
    │ ◀─── {CertificateVerify} ───────────│  ← 인증서 서명
    │ ◀─── {Finished} ────────────────────│
    │                                      │
    │──── {Finished} ────────────────────▶│
    │                                      │
    │ ↕ 이후 대칭키(AES-256-GCM)로 암호화 통신
```

TLS 1.2는 키 교환 후 추가 왕복이 필요했지만, TLS 1.3은 ClientHello에서 키 공유를 미리 제안하여 **1-RTT**로 핸드셰이크가 완료됩니다.

---

### TLS에서 PQC가 필요한 두 레이어

```
TLS 연결
  ├── 키 교환 (KEM 레이어)
  │     현재: ECDH (X25519)
  │     PQC:  X25519 + ML-KEM = 하이브리드 X25519MLKEM768
  │     → Shor 알고리즘으로 깨질 수 있는 부분 ✅ 실용 단계
  │
  └── 서버 인증서 (인증서 레이어)
        현재: RSA-2048 또는 ECDSA P-256 서명
        PQC:  ML-DSA 서명
        → 공개 PKI 인프라 전체가 지원해야 함 ⚠️ 수년 후
```

#### 왜 두 레이어 전환 속도가 다른가

**KEM 레이어**: 서버-클라이언트 협상이므로 서버 Nginx 설정 1줄이면 됩니다.
**인증서 레이어**: 루트 CA → 중간 CA → 서버 인증서 체인 전체가 ML-DSA를 지원해야 합니다. Let's Encrypt, 브라우저 신뢰 저장소, OS CA 번들이 모두 업데이트되어야 하며 수년이 소요됩니다.

---

### KEM (Key Encapsulation Mechanism) vs DH 키 교환

전통 TLS는 DH(Diffie-Hellman) 계열 키 교환을 사용합니다.

#### ECDH (X25519) 동작

```
서버: 개인키 s, 공개키 S = s·G
클라이언트: 개인키 c, 공개키 C = c·G

핸드셰이크:
  클라이언트 → 서버: C 전송
  서버 → 클라이언트: S 전송
  클라이언트: K = c·S = c·s·G
  서버: K = s·C = s·c·G  ← 동일한 K 도출

공유 비밀 K로 대칭키 유도 (HKDF)
```

#### ML-KEM (Kyber) 동작

KEM은 DH와 다르게 **캡슐화/역캡슐화** 방식입니다.

```
서버: ML-KEM 공개키 pk, 개인키 sk
클라이언트: Encapsulate(pk) → (ciphertext ct, 공유 비밀 K)
  클라이언트 → 서버: ct 전송
서버: Decapsulate(sk, ct) → 공유 비밀 K

동일한 K를 양쪽이 보유 → 대칭키 유도
```

KEM에서는 클라이언트만 ct를 생성할 수 있고, 서버 sk 없이는 K를 얻을 수 없습니다.

---

### 하이브리드 KEM — X25519MLKEM768

#### 왜 순수 ML-KEM이 아닌 하이브리드인가

```
위험 시나리오 (순수 ML-KEM만 사용):
  만약 ML-KEM에서 알려지지 않은 취약점이 발견된다면?
  → 모든 PQC 전환 트래픽이 즉시 위험

하이브리드 접근 (X25519 + ML-KEM):
  X25519가 안전하면: X25519만으로도 안전
  ML-KEM이 안전하면: ML-KEM으로 양자 저항 보장
  → 둘 중 하나만 안전해도 전체 보안 유지 (이중 안전망)
```

#### X25519MLKEM768 공유 비밀 결합 방법

```
X25519 공유 비밀:    K_classical (32B)
ML-KEM-768 공유 비밀: K_pqc (32B)
결합: K = HKDF(K_classical || K_pqc || transcript)

transcript = 핸드셰이크 메시지 해시
→ 두 공유 비밀이 모두 핸드셰이크 맥락에 결합되어 독립적 강도 보장
```

#### OpenSSL 3.5에서 지원하는 KEM 그룹

```bash
openssl list -kem-algorithms
# x25519
# x448
# mlkem512      ← ML-KEM-512 (FIPS 203, 보안 레벨 1)
# mlkem768      ← ML-KEM-768 (보안 레벨 3) ← QuSign
# mlkem1024     ← ML-KEM-1024 (보안 레벨 5)
# x25519mlkem768 ← 하이브리드 (권장)
```

**왜 oqs-provider가 아닌 OpenSSL 3.5 네이티브인가**:
- oqs-provider는 커뮤니티 유지 플러그인 — FIPS 인증 없음
- OpenSSL 3.5는 FIPS 203 표준 직접 구현 — 엔터프라이즈·금융 환경에서 신뢰 가능
- 장기 지원 보장 (oqs-provider는 OpenSSL 버전에 따라 호환성 깨짐)

---

### MTU 단편화 — 하이브리드 KEM의 실제 문제

#### ClientHello 크기 증가

```
X25519 key_share:            32B
X25519MLKEM768 key_share:   1,216B (ML-KEM-768 캡슐화 공개키 크기)
기타 헤더·확장:             ~300B

ClientHello 총 크기: ~1,600B
```

#### MTU(Maximum Transmission Unit) 문제

```
Ethernet MTU = 1,500B
IP 헤더 = 20B
TCP 헤더 = 20B
TLS 레코드 헤더 = 5B
페이로드 최대 = 1,455B

1,600B ClientHello → IP 단편화 발생
  패킷 1: 1,500B (첫 번째 조각)
  패킷 2:  100B (나머지)
```

일부 기업 방화벽·미들박스(DPI 장비)가 단편화된 TLS 패킷을 드롭합니다.
QUIC(UDP 기반)로 전환하면 단편화 문제가 줄어듭니다 (HTTP/3).

#### Nginx 설정 — 폴백 포함

```nginx
ssl_conf_command Groups X25519MLKEM768:X25519:P-256;
# 우선순위: X25519MLKEM768 → (미지원 클라이언트) X25519 → P-256

# 협상된 KEM 그룹 로그 (디버깅)
log_format pqc '$remote_addr "$ssl_protocol" "$ssl_cipher" curve="$ssl_curve"';
access_log /var/log/nginx/pqc.log pqc;
```

---

### ML-DSA 인증서 레이어 — 사설 CA 실험

공개 PKI에서 ML-DSA 인증서 발급이 불가능한 현재, 사설 CA로 동작 원리를 실험합니다.

#### TLS 인증서 체인 구조

```
루트 CA (자체 서명)
  └── 중간 CA (루트 CA가 서명)
       └── 서버 인증서 (중간 CA가 서명)

브라우저/OS: 루트 CA를 신뢰 저장소에 보유
  → 체인 검증: 서버 인증서 ← 중간 CA ← 루트 CA
  → 루트 CA가 신뢰 저장소에 있으면 체인 전체 신뢰
```

ML-DSA 사설 CA에서는 **루트 CA 인증서를 수동으로 신뢰 저장소에 등록**해야 합니다.

#### ML-DSA-65 사설 CA 구성

```bash
# 1. ML-DSA-65 루트 CA 키 + 자체 서명 인증서 생성
openssl genpkey -algorithm ML-DSA-65 -out root-ca.key
openssl req -new -x509 -key root-ca.key -out root-ca.crt -days 3650 \
  -subj "/CN=QuSign Test CA/O=QuSign/C=KR"

# 2. 서버 키 + CSR 생성
openssl genpkey -algorithm ML-DSA-65 -out server.key
openssl req -new -key server.key -out server.csr \
  -subj "/CN=qusign.link/O=QuSign"

# 3. 루트 CA로 서버 인증서 서명
openssl x509 -req -in server.csr \
  -CA root-ca.crt -CAkey root-ca.key \
  -out server.crt -days 365 \
  -extensions v3_req

# 4. 인증서 크기 확인
openssl x509 -in server.crt -text | grep "Public Key Size"
wc -c < server.crt  # 파일 크기(B)
```

#### 인증서 크기 비교 (실측 대상)

| | RSA-2048 | ECDSA P-256 | ML-DSA-65 |
|---|---|---|---|
| 공개키 크기 | ~270B | 65B | **1,952B** |
| 서명 크기 | 256B | ~72B | **3,309B** |
| 인증서 전체 | ~1.2KB | ~0.5KB | **~5.5KB** |
| 핸드셰이크 추가 비용 | 기준 | 작음 | **~5KB 증가** |
| 첫 연결 지연 (추정) | 기준 | 미미 | **+수십 ms** |

5KB 증가는 TLS 핸드셰이크를 여러 TCP 패킷으로 분할시켜 RTT 증가를 유발합니다.

#### mTLS (상호 인증) 실험

```bash
# 서버: 클라이언트 인증서 요구
openssl s_server \
  -cert server.crt -key server.key \
  -CAfile root-ca.crt -Verify 1 \
  -port 4433

# 클라이언트: ML-DSA 클라이언트 인증서 제시
openssl s_client \
  -connect localhost:4433 \
  -cert client.crt -key client.key \
  -CAfile root-ca.crt
```

---

### 미래 PKI 기술 방향

#### Merkle Tree Certificates (Google/Let's Encrypt)

ML-DSA 인증서가 ~5.5KB로 커지는 문제를 해결하기 위해 논의 중인 방식:
- 인증서 서명 대신 머클 트리 포함 증명(inclusion proof)으로 대체
- 클라이언트가 짧은 머클 경로만 검증 → 전송 크기 대폭 감소
- 단기 인증서(수 시간 유효)를 빠르게 발급하는 방향

#### Composite 인증서

전환기 방식: 인증서 하나에 RSA 서명 + ML-DSA 서명 모두 포함:
```
인증서 = RSA 서명 || ML-DSA 서명
  - PQC 미지원 클라이언트: RSA 서명으로 검증
  - PQC 지원 클라이언트:  ML-DSA 서명으로 검증
```

---

### QuSign crypto-agility 설계

서명 알고리즘을 교체 가능한 구조로 추상화합니다.

```kotlin
// 인터페이스 추출
interface SignatureAlgorithm {
    val algorithmName: String
    fun generateKeyPair(): KeyPair
    fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray
    fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean
    fun publicKeyFromBytes(bytes: ByteArray): PublicKey
    fun privateKeyFromBytes(bytes: ByteArray): PrivateKey
}

// ML-DSA-65 구현체
@Component
@ConditionalOnProperty("pqc.signature.algorithm", havingValue = "ML-DSA-65", matchIfMissing = true)
class MlDsa65Algorithm : SignatureAlgorithm {
    override val algorithmName = "ML-DSA-65"

    override fun generateKeyPair(): KeyPair {
        val gen = KeyPairGenerator.getInstance("ML-DSA", "BC")
        gen.initialize(MLDSAParameterSpec.ml_dsa_65)
        return gen.generateKeyPair()
    }

    override fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray {
        val signer = Signature.getInstance("ML-DSA", "BC")
        signer.initSign(privateKey)
        signer.update(data)
        return signer.sign()
    }

    override fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean {
        val verifier = Signature.getInstance("ML-DSA", "BC")
        verifier.initVerify(publicKey)
        verifier.update(data)
        return verifier.verify(signature)
    }
}
```
(`"ML-DSA"`/`MLDSAParameterSpec.ml_dsa_65`는 실제 `BouncyCastlePqcSignatureService.kt`와 동일한 명칭입니다 — 구 BouncyCastle의 `"DILITHIUM"`/`DilithiumParameterSpec.dilithium3` 명칭은 FIPS 204 표준화 이후 폐기되었습니다. [[06-pqc-mldsa]] 참고.)

```yaml
# application.yml
pqc:
  signature:
    algorithm: ML-DSA-65  # 향후 ML-DSA-87 또는 JDK 네이티브로 변경 시 이 값만 수정
```

#### JDK 로드맵

| JEP | 내용 | 상태 |
|---|---|---|
| JEP 496 | ML-KEM (`javax.crypto.KEM`) | JDK 24 Preview |
| JEP 497 | ML-DSA (`java.security.Signature`) | JDK 24 Preview |
| JEP 527 | TLS 하이브리드 KEM | 논의 중 |

JEP 497이 최종 표준화되면 `Signature.getInstance("ML-DSA-65")` 로 BouncyCastle 없이 사용 가능합니다.
`SignatureAlgorithm` 인터페이스가 있으면 구현체만 교체하고 서비스 코드는 그대로 유지됩니다.

---

## 데모 증명 — "연결된다"가 아닌 "PQC로 협상됐다는 증거"

### Chrome DevTools 확인

```
DevTools → Security 탭 → Connection
"Key exchange: X25519MLKEM768" 표시 확인 → 스크린샷 저장
```

Chrome 124+는 X25519MLKEM768을 기본 활성화합니다.
서버가 지원하면 자동으로 협상됩니다.

### tshark 패킷 캡처

```bash
# TLS 핸드셰이크 캡처
tshark -i eth0 -f "port 443" -w handshake.pcap

# ClientHello key_share extension 분석
tshark -r handshake.pcap \
  -Y "tls.handshake.type == 1" \
  -V | grep -A10 "Key Share"

# 예상 출력:
# Key Share Entry: Group: X25519MLKEM768
#   Key Exchange Length: 1216
#   Key Exchange: ...
```

### 오버헤드 직접 측정

```bash
# X25519 핸드셰이크 시간 측정
time openssl s_client -connect qusign.link:443 -groups x25519 < /dev/null

# X25519MLKEM768 핸드셰이크 시간 측정
time openssl s_client -connect qusign.link:443 -groups x25519mlkem768 < /dev/null
```

---

## 확인 질문 & 답변

**Q1. TLS에서 키 교환(KEM)은 PQC가 실용 단계인데 인증서 서명은 아직인 이유는?**

> KEM은 서버 Nginx 설정 1줄이면 즉시 적용 가능합니다. 반면 인증서 서명은 루트 CA → 중간 CA → 서버 인증서 전체 체인이 ML-DSA를 지원해야 하고, 브라우저와 OS의 신뢰 저장소도 ML-DSA 루트 CA를 포함해야 합니다. Let's Encrypt, 주요 브라우저, OS 벤더 모두의 업데이트와 상호운용성 검증이 필요해 수년이 소요됩니다.

**Q2. KEM과 DH 키 교환의 근본적 차이는?**

> DH는 양측이 각자 공개값을 교환하여 공동으로 공유 비밀을 계산합니다(대화형). KEM은 한쪽(클라이언트)이 공개키로 공유 비밀을 암호화(캡슐화)하여 전송하고, 다른쪽(서버)이 개인키로 복호화(역캡슐화)합니다(비대화형). KEM은 서버 공개키 하나만 있으면 공유 비밀 수립이 가능해 프로토콜 설계가 단순해집니다.

**Q3. "Harvest Now, Decrypt Later"가 TLS 키 교환에 미치는 구체적 위협은?**

> 공격자가 오늘 TLS 트래픽(암호화된 API 요청, PDF 전송)을 저장합니다. 10년 후 충분한 양자 컴퓨터로 X25519 개인키를 복원하면, 저장해둔 트래픽을 모두 복호화할 수 있습니다. X25519MLKEM768 하이브리드를 사용하면 X25519가 깨져도 ML-KEM이 보호하므로, 저장된 트래픽을 양자 컴퓨터로도 복호화할 수 없습니다.

**Q4. X25519MLKEM768에서 ClientHello가 1,600B인 것이 왜 문제인가?**

> Ethernet MTU는 1,500B입니다. 1,600B ClientHello는 IP 단편화가 발생합니다. 일부 기업 방화벽·DPI(Deep Packet Inspection) 장비가 단편화된 TLS 패킷을 보안 정책으로 드롭합니다. 결과적으로 특정 기업 네트워크에서 HTTPS 연결이 완전히 실패합니다. Nginx에서 `X25519MLKEM768:X25519` 폴백 설정이 필수인 이유입니다.

**Q5. QuSign에서 `SignatureAlgorithm` 인터페이스를 도입하는 것이 현 단계에서 실제로 필요한가?**

> 기능적으로는 현재 ML-DSA-65 고정으로 충분합니다. 이 추상화는 두 가지 가치가 있습니다: (1) JEP 497이 JDK 표준으로 나왔을 때 BouncyCastle을 제거하고 JDK 네이티브로 교체할 수 있는 구조. (2) 면접에서 "왜 이 구조인가" 질문에 "알고리즘 민첩성(crypto-agility)을 설계 초기부터 반영했다"고 답할 수 있는 포트폴리오 서사. 실질적 가치와 서사 가치 모두 있습니다.

**Q6. ML-DSA 인증서 크기 ~5.5KB가 TLS 성능에 미치는 실제 영향은?**

> TLS 핸드셰이크는 연결당 1번입니다. 이후 keep-alive로 연결을 재사용하면 추가 핸드셰이크가 없습니다. 1회 5KB 추가는 현대 네트워크에서 수십 ms 지연입니다. 하지만 CDN·API 게이트웨이처럼 초당 수천 개 신규 연결이 있는 환경에서는 누적 영향이 큽니다. Merkle Tree Certificates처럼 인증서 크기를 줄이는 기술이 논의 중인 이유입니다.
