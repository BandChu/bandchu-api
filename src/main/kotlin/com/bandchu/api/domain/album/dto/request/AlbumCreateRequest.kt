package com.bandchu.api.domain.album.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
@Schema(description = "앨범 정보 넣기 요청")
data class AlbumCreateRequest(
    @get:Schema(description = "앨범명", example = "HEADACHE (데이먼스 이어 앨범입니다.)")
    @field:NotBlank(message = "앨범명을 입력하세요.")
    @field:Size(max = 30, message = "앨범명은 최대 30자까지 입력 가능합니다.")
    val name: String,

    @get:Schema(description = "앨범 커버 이미지 url", example = "https://image.genie.co.kr/Y/IMAGE/IMG_ALBUM/081/865/999/81865999_1611795925768_1_600x600.JPG")
    val coverImageUrl: String?,

    @get:Schema(description = "앨범 발매 날짜", example = "2026년 1월 9일")
    @field:NotBlank(message = "발매일은 필수 입력 항목입니다.")
    val releaseDate: String,

    @get:Schema(description = "앨범 설명", example = "데이먼스 이어의 첫 EP 앨범")
    val description: String?,

    @get:Schema(description = "앨범 트랙 리스트", example = "앨범 Track request 를 리스트로 담슴당")
    @field:Valid
    val tracks: List<TrackRequest> = emptyList()
)
