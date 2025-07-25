package com.example.mysilgurae.data

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class ApiResponse(
    @field:Element(name = "body")
    var body: Body? = null
)

@Root(name = "body", strict = false)
data class Body(
    @field:Element(name = "items")
    var items: Items? = null
)

@Root(name = "items", strict = false)
data class Items(
    @field:ElementList(inline = true, name = "item")
    var itemList: List<ApartmentDeal>? = null
)

@Root(name = "item", strict = false)
data class ApartmentDeal(
    @field:Element(name = "dealAmount", required = false)
    var dealAmount: String? = null,
    @field:Element(name = "buildYear", required = false)
    var buildYear: Int? = null,
    @field:Element(name = "dealYear", required = false)
    var dealYear: Int? = null,
    @field:Element(name = "dealMonth", required = false)
    var dealMonth: Int? = null,
    @field:Element(name = "dealDay", required = false)
    var dealDay: Int? = null,
    @field:Element(name = "umdNm", required = false)
    var dong: String? = null,
    @field:Element(name = "aptNm", required = false)
    var apartmentName: String? = null,
    @field:Element(name = "excluUseAr", required = false)
    var area: Double? = null,
    @field:Element(name = "jibun", required = false)
    var jibun: String? = null,
    @field:Element(name = "floor", required = false)
    var floor: Int? = null,

    // [추가!] 변환된 좌표를 저장할 변수
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    val displayName: String
        get() = apartmentName ?: "이름 정보 없음"

    val formattedPrice: String
        get() = dealAmount?.trim() ?: "정보 없음"

    val fullAddress: String
        get() = "$dong $jibun"

    // [추가!] 전용면적을 평으로 변환하는 프로퍼티
    val areaInPyeong: Int
        get() = area?.let { (it / 3.305785).toInt() } ?: 0
}