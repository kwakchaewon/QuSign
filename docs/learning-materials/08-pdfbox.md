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
| `QuSign-Signature` | Base64(ML-DSA 서명 bytes) | 서명값 |
| `QuSign-SignerId` | `signer@example.com` | 서명자 식별 (이메일을 ID로 사용) |
| `QuSign-SignedAt` | ISO-8601 (`2025-01-15T09:30:00Z`) | 서명 일시 |
| `QuSign-DocumentHash` | Base64(SHA3-256) | 원본 문서 해시 |
| `QuSign-SignerIP` | `192.168.1.1` | 감사 추적용 IP |

공개키는 PDF에 넣지 않습니다. 검증 시 `QuSign-SignerId`로 DB에서 사용자를 조회해 그 사용자의 `publicKey` 컬럼을 사용합니다 — 즉 QuSign 검증은 서버(DB) 의존적이며, PDF 파일만으로 오프라인 검증은 불가능합니다.

### 왜 PDF 본문이 아닌 메타데이터인가

- **비파괴**: 본문을 수정하면 원본 해시가 바뀌어 무결성 검증이 불가능합니다.
- **표준 위치**: 메타데이터는 PDF 리더에서 별도 탭으로 조회 가능합니다.
- **접근 용이**: 서명 추출 시 전체 문서를 파싱할 필요 없이 메타데이터만 읽으면 됩니다.

---

## 현재 코드에서의 사용 예시

### 서명 삽입 — `PdfBoxSignatureService.kt`
```kotlin
override fun embedSignature(
    pdfBytes: ByteArray,
    signature: ByteArray,
    signerId: String,
    documentHash: ByteArray,
    ipAddress: String,
): ByteArray {
    Loader.loadPDF(pdfBytes).use { doc ->
        val info = doc.documentInformation
        info.setCustomMetadataValue(KEY_SIGNATURE, Base64.getEncoder().encodeToString(signature))
        info.setCustomMetadataValue(KEY_SIGNER_ID, signerId)
        info.setCustomMetadataValue(KEY_SIGNED_AT, Instant.now().toString())
        info.setCustomMetadataValue(KEY_DOC_HASH, Base64.getEncoder().encodeToString(documentHash))
        info.setCustomMetadataValue(KEY_SIGNER_IP, ipAddress)

        return ByteArrayOutputStream().also { doc.save(it) }.toByteArray()
    }
}
```
`.use { }` — `Closeable`을 안전하게 닫아주는 Kotlin 확장 함수입니다. (`try-finally` 대체)
`Loader.loadPDF()`는 PDFBox 3.x의 로딩 API입니다 (2.x의 `PDDocument.load()`는 3.x에서 제거되었습니다).

### 서명 추출 — `PdfBoxSignatureService.kt`
```kotlin
override fun extractMetadata(pdfBytes: ByteArray): SignatureMetadata? {
    Loader.loadPDF(pdfBytes).use { doc ->
        val info = doc.documentInformation
        val b64      = info.getCustomMetadataValue(KEY_SIGNATURE)  ?: return null
        val signerId = info.getCustomMetadataValue(KEY_SIGNER_ID)  ?: return null
        val signedAt = info.getCustomMetadataValue(KEY_SIGNED_AT)  ?: return null
        val hashB64  = info.getCustomMetadataValue(KEY_DOC_HASH)   ?: return null
        return SignatureMetadata(
            signature    = Base64.getDecoder().decode(b64),
            signerId     = signerId,
            signedAt     = signedAt,
            documentHash = Base64.getDecoder().decode(hashB64),
        )
    }
}
```

### 무결성 검증 흐름 — `SignatureFlowService.kt:728-739`
```
서명된 PDF → 메타데이터에서 signature·signerId·documentHash 추출
signerId(이메일)로 DB에서 사용자 조회 → publicKey 컬럼 로드
ML-DSA.verify(publicKey, documentHash, signature) → true/false
```

공개키는 PDF가 아니라 **DB**에서 가져옵니다. `documentHash`는 서명 삽입 전 원본 파일의 SHA3-256 해시이며, 메타데이터 안에 함께 봉인되어 있어 서명된 PDF 자체만으로 "무엇에 대한 서명인지"를 알 수 있습니다.

---

## 확인 질문 & 답변

**Q1. `PDDocument.load()`를 `.use { }` 블록 안에서 쓰는 이유는?**

> PDDocument는 `Closeable`을 구현합니다. `.use { }` 블록이 끝나면 예외 발생 여부와 관계없이 `close()`가 자동 호출됩니다. PDF 파일 핸들이 누수되면 메모리와 파일 디스크립터가 소진됩니다.

**Q2. 서명된 PDF를 다시 업로드하면 무결성 검증에 실패하는 이유는?**

> 서명 삽입 후 PDF 파일 자체가 변경되므로 SHA3-256 해시값이 달라집니다. DB에는 서명 전 원본의 해시가 저장되어 있어 "이미 서명된 PDF"를 감지하고 차단합니다 (4단계 중복 업로드 방지 기능).

**Q3. PDF 메타데이터에 넣는 `QuSign-Signature` 값이 Base64인 이유는?**

> ML-DSA 서명값은 이진(binary) 바이트 배열입니다. PDF 메타데이터는 UTF-8 텍스트만 저장할 수 있으므로 Base64로 인코딩하여 ASCII 문자열로 변환합니다. 추출 시 Base64 디코딩으로 원본 바이트를 복원합니다.

**Q4. 검증 시 공개키를 PDF가 아니라 DB에서 가져오는 이유는? 단점은 없는가?**

> QuSign은 공개키를 PDF에 넣지 않고, 메타데이터의 `QuSign-SignerId`(서명자 이메일)로 DB를 조회해 공개키를 가져옵니다. 이렇게 하면 사용자가 키를 분실·재발급했을 때 DB의 최신 공개키 기준으로 검증할 수 있고, PDF에 공개키 원문을 노출하지 않아도 됩니다. 단점은 **오프라인(서비스 종료 후) 검증이 불가능**하다는 점입니다 — PDF 파일만으로는 서명을 검증할 수 없고 QuSign DB가 살아있어야 합니다.
