# PQC 하이브리드 TLS (8-4단계)

> PLAN.md §8-4 대응 — ML-KEM + X25519 하이브리드 TLS, ML-DSA 사설 CA

---

## 이론

### TLS 핸드셰이크 구조

```
클라이언트                          서버
    │── ClientHello (key_share) ──▶ │  ← 지원하는 키 교환 알고리즘 목록
    │ ◀── ServerHello (key_share) ──│  ← 선택된 알고리즘
    │ ◀── Certificate ─────────────│  ← 서버 인증서 (공개키 포함)
    │── Finished (대칭키로 암호화) ─▶│
    │ ◀── Finished ─────────────────│
    │  (이후 대칭키로 암호화 통신)
```

TLS에서 PQC는 두 레이어에서 필요합니다:

| 레이어 | 현재 | PQC 전환 | 상태 |
|---|---|---|---|
| **키 교환 (KEM)** | X25519 (ECDH) | X25519MLKEM768 (하이브리드) | ✅ 실용 단계 |
| **인증서 서명** | RSA/ECDSA | ML-DSA | ⚠️ PKI 인프라 미지원 |

### 왜 하이브리드인가 (X25519MLKEM768)

"순수 ML-KEM"이 아니라 "X25519 + ML-KEM 조합"을 쓰는 이유:
1. **하위 호환**: ML-KEM을 지원하지 않는 클라이언트가 X25519로 폴백 가능
2. **이중 보안**: 두 알고리즘 중 하나만 안전해도 전체 보안이 유지됨
3. **양자 전환기**: 고전 컴퓨터가 여전히 대다수인 현재, 점진적 전환을 위한 현실적 선택

### OpenSSL 3.5 — 네이티브 ML-KEM 지원

```bash
# oqs-provider(외부 플러그인) 불필요 — 3.5에서 기본 내장
openssl version         # 3.5+ 확인
openssl list -kem-algorithms | grep ML-KEM

# Nginx 설정
ssl_conf_command Groups X25519MLKEM768:X25519;
# ↑ X25519MLKEM768 우선, PQC 미지원 클라이언트는 X25519로 자동 폴백
```

왜 oqs-provider가 아닌 OpenSSL 3.5 네이티브인가?
- oqs-provider는 비공식 플러그인 — FIPS 인증 없음, 장기 지원 불확실
- OpenSSL 3.5는 FIPS 204 표준 구현 — 엔터프라이즈 환경에서 신뢰 가능

---

## 데모 증명 — "연결된다"가 아닌 "PQC로 협상됐다는 증거"

### Chrome DevTools 확인

```
DevTools → Security 탭 → Connection
"Key exchange: X25519MLKEM768" 표시 확인 → 스크린샷 저장
```

### tshark 패킷 캡처

```bash
# ClientHello key_share에 ML-KEM 바이트 확인
tshark -i eth0 -f "port 443" -w handshake.pcap
tshark -r handshake.pcap -Y "tls.handshake.type == 1" -V | grep -A5 "Key Share"
```

### 오버헤드 비교표

| | X25519 (고전) | X25519MLKEM768 (하이브리드) |
|---|---|---|
| ClientHello 크기 | ~300B | ~1,600B (+약 1,200B) |
| 핸드셰이크 지연 | 기준 | 거의 동일 (~1ms 미만) |
| 보안 레벨 | 128-bit classical | 128-bit classical + 128-bit quantum |

1,200B 증가는 MTU(1,500B)를 초과하여 IP 단편화가 발생할 수 있습니다.
일부 미들박스·방화벽이 분할 패킷을 드롭하는 시나리오 이해가 필요합니다.

---

## ML-DSA 사설 CA (인증서 레이어)

공개 PKI(Let's Encrypt, AWS ACM)는 아직 ML-DSA 인증서를 발급하지 않습니다.
사설 CA로 직접 실험할 수 있습니다:

```bash
# ML-DSA-65 루트 CA 생성
openssl genpkey -algorithm ML-DSA-65 -out root-ca.key
openssl req -new -x509 -key root-ca.key -out root-ca.crt -days 3650 \
  -subj "/CN=QuSign Test CA/O=QuSign"

# 서버 인증서 생성 및 CA 서명
openssl genpkey -algorithm ML-DSA-65 -out server.key
openssl req -new -key server.key -out server.csr -subj "/CN=qusign.link"
openssl x509 -req -in server.csr -CA root-ca.crt -CAkey root-ca.key \
  -out server.crt -days 365
```

### 인증서 크기 비교 (직접 측정 대상)

| | RSA-2048 | ECDSA P-256 | ML-DSA-65 |
|---|---|---|---|
| 공개키 크기 | ~270B | 65B | **1,952B** |
| 서명 크기 | 256B | ~72B | **3,309B** |
| 핸드셰이크 추가 비용 | 기준 | 작음 | **~5KB 증가** |

5KB 증가는 TLS 핸드셰이크를 여러 TCP 패킷으로 분할시켜 첫 연결 지연을 높입니다.

---

## QuSign crypto-agility 설계

서명 알고리즘을 교체 가능한 구조로 추상화합니다:

```kotlin
// 현재: 하드코딩된 ML-DSA-65
// 목표: 설정 기반 알고리즘 교체

interface SignatureAlgorithm {
    fun generateKeyPair(): KeyPair
    fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray
    fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean
}

class MlDsa65Algorithm : SignatureAlgorithm {
    override fun generateKeyPair(): KeyPair {
        val gen = KeyPairGenerator.getInstance("DILITHIUM", "BC")
        gen.initialize(DilithiumParameterSpec.dilithium3)
        return gen.generateKeyPair()
    }
    // ...
}

// application.yml
// pqc.signature.algorithm: ML-DSA-65
// → Spring @ConditionalOnProperty로 구현체 선택
```

JDK 로드맵:
- JEP 496 (ML-KEM) — JDK 24 Preview
- JEP 527 (TLS 하이브리드) — 논의 중
- liboqs-java → 표준 라이브러리 출시 시 교체 가능한 구조가 되어야 합니다.

---

## 확인 질문 & 답변

**Q1. TLS에서 키 교환(KEM)은 PQC가 실용 단계인데 인증서 서명은 아직인 이유는?**

> KEM은 서버-클라이언트 협상으로 즉시 적용 가능합니다. 하지만 인증서 서명은 공개 PKI(루트 CA → 중간 CA → 서버 인증서)의 전체 체인이 ML-DSA를 지원해야 합니다. Let's Encrypt·브라우저·OS 신뢰 저장소가 모두 업데이트되어야 합니다. 인프라 전환 시간 때문에 인증서 레이어는 수년이 더 걸릴 전망입니다.

**Q2. ML-DSA 인증서가 핸드셰이크에 추가하는 ~5KB가 실제 서비스에 미치는 영향은?**

> TLS 핸드셰이크는 연결당 1번만 발생하고 이후 대칭키로 통신합니다. 1회 5KB 추가는 현대 네트워크에서 수십 ms 정도 지연입니다. 하지만 HTTPS 연결이 많은 CDN·API 게이트웨이에서는 누적 영향이 있습니다. Merkle Tree Certificates 같은 압축 기술이 논의 중인 이유입니다.

**Q3. X25519MLKEM768에서 ClientHello가 1,600B인 것이 왜 문제인가?**

> Ethernet MTU는 1,500B입니다. 1,600B ClientHello는 IP 단편화(fragmentation)가 발생합니다. 일부 기업 방화벽·미들박스가 단편화된 TLS 패킷을 보안 정책으로 드롭합니다. 결과적으로 특정 네트워크에서 HTTPS 연결이 완전히 실패합니다. TLS는 QUIC(UDP 기반)으로 전환하면 단편화 문제가 줄어듭니다.

**Q4. QuSign에서 `SignatureAlgorithm` 인터페이스를 도입하는 것이 현 단계에서 실제로 필요한가?**

> 기능적으로는 현재 ML-DSA-65 고정으로 충분합니다. 이 추상화는 "JDK 표준 PQC 라이브러리가 나왔을 때 liboqs-java를 제거하고 교체할 수 있다"는 포트폴리오 서사를 위한 설계입니다. 면접에서 "왜 이 구조인가"에 대한 답변이 됩니다: 알고리즘 민첩성(crypto-agility)을 처음부터 설계에 반영했다는 것.
