package com.qusign.document.controller

import com.qusign.common.response.ApiResponse
import com.qusign.document.dto.DocumentResponse
import com.qusign.document.service.DocumentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Tag(name = "Document", description = "문서 업로드 · 목록 · 다운로드")
@RestController
@RequestMapping("/api/documents")
class DocumentController(private val documentService: DocumentService) {

    @Operation(summary = "문서 업로드", description = "PDF 파일 1개를 업로드합니다. 중복 서명 문서는 거부됩니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "업로드 성공"),
        SwaggerApiResponse(responseCode = "400", description = "PDF 형식이 아니거나 파일이 유효하지 않음"),
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "업로드할 PDF 파일", required = true)
        @RequestParam("file") file: MultipartFile,
    ): ApiResponse<DocumentResponse> = ApiResponse.ok(documentService.upload(email, file))

    @Operation(summary = "문서 배치 업로드", description = "PDF 파일 최대 5개를 한 번에 업로드합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "배치 업로드 성공"),
        SwaggerApiResponse(responseCode = "400", description = "파일 수 초과(5개 이상) 또는 PDF 형식 오류"),
    )
    @PostMapping("/batch", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadBatch(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "업로드할 PDF 파일 목록 (최대 5개)", required = true)
        @RequestParam("files") files: List<MultipartFile>,
    ): ApiResponse<List<DocumentResponse>> = ApiResponse.ok(documentService.uploadBatch(email, files))

    @Operation(summary = "내 문서 목록 조회", description = "로그인 사용자가 업로드한 문서 전체 목록을 반환합니다.")
    @GetMapping
    fun list(@AuthenticationPrincipal email: String): ApiResponse<List<DocumentResponse>> =
        ApiResponse.ok(documentService.list(email))

    @Operation(summary = "문서 다운로드", description = "원본 PDF를 다운로드합니다. 본인 문서만 다운로드 가능합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "PDF 파일 스트림", content = [Content(mediaType = "application/pdf")]),
        SwaggerApiResponse(responseCode = "403", description = "타인 문서 접근 시도"),
        SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없음"),
    )
    @GetMapping("/{id}/download")
    fun download(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "문서 ID", required = true, example = "1")
        @PathVariable id: Long,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = documentService.download(email, id)
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }
}
