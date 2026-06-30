# PDFBox — PDF 서명 삽입·추출 (1-3단계)

> PLAN.md §1-3 대응 — ML-DSA 서명값을 PDF 메타데이터에 넣고 꺼낸다

---

## 이론

### PDFBox 핵심 구조

```
PDDocument          ← 파일 전체를 메모리에 로드
  └── PDDocumentInformation   ← 문서 메타데이터 (제목·작성자·Custom 속성)
  └── PDPage                  ← 개별 페이지
```

QuSign은 PDFBox를 사용해 **커스텀 메타데이터 필드**에 ML-DSA 서명 정보를 삽입합니다.
PDF 시각적 내용(본문)은 변경하지 않아 원본 해시와 서명 후 해시를 분리할 수 있습니다.

### PDF 메타데이터 구조

표준 필드: `Title`, `Author`, `Subject`, `Keywords`, `Producer`, `Creator`, `CreationDate`, `ModDate`

`PDDocumentInformation.setCustomMetadataValue(key, value)` 로 커스텀 필드를 자유롭게 추가할 수 있습니다.

### QuSign 메타데이터 설계

| 메타데이터 키 | 값 형식 | 설명 |
|---|---|---|
| `X-Signature` | Base64(ML-DSA 서명 bytes) | 서명값 |
| `X-SignerPublicKey` | Base64(공개키 bytes) | 검증용 공개키 |
| `X-SignedAt` | ISO-8601 (`2025-01-15T09:30:00Z`) | 서명 일시 |
| `X-SignerEmail` | `signer@example.com` | 서명자 식별 |
| `X-SignerIP` | `192.168.1.1` | 감사 추적용 IP |
| `X-DocumentHash` | Hex(SHA3-256) | 원본 문서 해시 |

### 왜 PDF 본문이 아닌 메타데이터인가

- **비파괴**: 본문을 수정하면 원본 해시가 바뀌어 무결성 검증이 불가능합니다.
- **표준 위치**: 메타데이터는 PDF 리더에서 별도 탭으로 조회 가능합니다.
- **접근 용이**: 서명 추출 시 전체 문서를 파싱할 필요 없이 메타데이터만 읽으면 됩니다.

---

## 현재 코드에서의 사용 예시

### 서명 삽입 — `PdfBoxSignatureService.kt`
```kotlin
fun embedSignature(
    pdfBytes: ByteArray,
    signature: ByteArray,
    publicKey: PublicKey,
    signerEmail: String,
    ipAddress: String
): ByteArray {
    PDDocument.load(pdfBytes).use { doc ->
        val info = doc.documentInformation
        info.setCustomMetadataValue("X-Signature", Base64.getEncoder().encodeToString(signature))
        info.setCustomMetadataValue("X-SignerPublicKey", Base64.getEncoder().encodeToString(publicKey.encoded))
        info.setCustomMetadataValue("X-SignedAt", Instant.now(ZoneOffset.UTC).toString())
        info.setCustomMetadataValue("X-SignerEmail", signerEmail)
        info.setCustomMetadataValue("X-SignerIP", ipAddress)

        val outputStream = ByteArrayOutputStream()
        doc.save(outputStream)
        return outputStream.toByteArray()
    }
}
```
`.use { }` — `Closeable`을 안전하게 닫아주는 Kotlin 확장 함수입니다. (`try-finally` 대체)

### 서명 추출 — `PdfBoxSignatureService.kt`
```kotlin
fun extractMetadata(pdfBytes: ByteArray): PdfSignatureMetadata {
    PDDocument.load(pdfBytes).use { doc ->
        val info = doc.documentInformation
        return PdfSignatureMetadata(
            signature = Base64.getDecoder().decode(
                info.getCustomMetadataValue("X-Signature") ?: throw SignatureNotFoundException()
            ),
            publicKeyBytes = Base64.getDecoder().decode(
                info.getCustomMetadataValue("X-SignerPublicKey") ?: throw SignatureNotFoundException()
            ),
            signedAt = info.getCustomMetadataValue("X-SignedAt"),
            signerEmail = info.getCustomMetadataValue("X-SignerEmail")
        )
    }
}
```

### 무결성 검증 흐름
```
원본 PDF → SHA3-256 해시 → DB 저장 (업로드 시)
서명된 PDF → 메타데이터에서 서명값·공개키 추출 → ML-DSA 검증
```

서명값 검증 대상은 **서명 삽입 전 원본 파일의 해시**입니다.
이 해시는 DB에 별도 저장되어 있어 서명된 PDF의 메타데이터와 비교·검증합니다.

---

## 확인 질문 & 답변

**Q1. `PDDocument.load()`를 `.use { }` 블록 안에서 쓰는 이유는?**

> PDDocument는 `Closeable`을 구현합니다. `.use { }` 블록이 끝나면 예외 발생 여부와 관계없이 `close()`가 자동 호출됩니다. PDF 파일 핸들이 누수되면 메모리와 파일 디스크립터가 소진됩니다.

**Q2. 서명된 PDF를 다시 업로드하면 무결성 검증에 실패하는 이유는?**

> 서명 삽입 후 PDF 파일 자체가 변경되므로 SHA3-256 해시값이 달라집니다. DB에는 서명 전 원본의 해시가 저장되어 있어 "이미 서명된 PDF"를 감지하고 차단합니다 (4단계 중복 업로드 방지 기능).

**Q3. PDF 메타데이터에 넣는 `X-Signature` 값이 Base64인 이유는?**

> ML-DSA 서명값은 이진(binary) 바이트 배열입니다. PDF 메타데이터는 UTF-8 텍스트만 저장할 수 있으므로 Base64로 인코딩하여 ASCII 문자열로 변환합니다. 추출 시 Base64 디코딩으로 원본 바이트를 복원합니다.

**Q4. 원본 문서 해시를 메타데이터의 `X-DocumentHash`에 저장하는 동시에 DB에도 저장하는 이유는?**

> DB는 서비스 내부 검증용, 메타데이터는 서비스 외부(오프라인) 검증용입니다. PDF 파일만 있어도 포함된 공개키로 서명을 직접 검증할 수 있어 서비스 종속성을 낮춥니다.
