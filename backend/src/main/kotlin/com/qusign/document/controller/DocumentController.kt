package com.qusign.document.controller

import com.qusign.common.response.ApiResponse
import com.qusign.document.dto.DocumentResponse
import com.qusign.document.service.DocumentService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/documents")
class DocumentController(private val documentService: DocumentService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @AuthenticationPrincipal email: String,
        @RequestParam("file") file: MultipartFile,
    ): ApiResponse<DocumentResponse> = ApiResponse.ok(documentService.upload(email, file))

    @PostMapping("/batch", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadBatch(
        @AuthenticationPrincipal email: String,
        @RequestParam("files") files: List<MultipartFile>,
    ): ApiResponse<List<DocumentResponse>> = ApiResponse.ok(documentService.uploadBatch(email, files))

    @GetMapping
    fun list(@AuthenticationPrincipal email: String): ApiResponse<List<DocumentResponse>> =
        ApiResponse.ok(documentService.list(email))

    @GetMapping("/{id}/download")
    fun download(
        @AuthenticationPrincipal email: String,
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
