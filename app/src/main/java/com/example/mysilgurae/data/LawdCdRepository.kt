package com.example.mysilgurae.data

object LawdCdRepository {

    private val seoulLawdCdMap = mapOf(
        "종로구" to "11110", "중구" to "11140", "용산구" to "11170", "성동구" to "11200",
        "광진구" to "11215", "동대문구" to "11230", "중랑구" to "11260", "성북구" to "11290",
        "강북구" to "11305", "도봉구" to "11320", "노원구" to "11350", "은평구" to "11380",
        "서대문구" to "11410", "마포구" to "11440", "양천구" to "11470", "강서구" to "11500",
        "구로구" to "11530", "금천구" to "11545", "영등포구" to "11560", "동작구" to "11590",
        "관악구" to "11620", "서초구" to "11650", "강남구" to "11680", "송파구" to "11710",
        "강동구" to "11740"
    )

    private val gyeonggiLawdCdMap = mapOf(
        "수원시 장안구" to "41111", "수원시 권선구" to "41113", "수원시 팔달구" to "41115", "수원시 영통구" to "41117",
        "성남시 수정구" to "41131", "성남시 중원구" to "41133", "성남시 분당구" to "41135",
        "의정부시" to "41150", "안양시 만안구" to "41171", "안양시 동안구" to "41173",
        "부천시" to "41190", "광명시" to "41210", "평택시" to "41220", "동두천시" to "41250",
        "안산시 상록구" to "41271", "안산시 단원구" to "41273", "고양시 덕양구" to "41281",
        "고양시 일산동구" to "41285", "고양시 일산서구" to "41287", "과천시" to "41290",
        "구리시" to "41310", "남양주시" to "41360", "오산시" to "41370", "시흥시" to "41390",
        "군포시" to "41410", "의왕시" to "41430", "하남시" to "41450", "용인시 처인구" to "41461",
        "용인시 기흥구" to "41463", "용인시 수지구" to "41465", "파주시" to "41480",
        "이천시" to "41500", "안성시" to "41550", "김포시" to "41570", "화성시" to "41590",
        "광주시" to "41610", "양주시" to "41630", "포천시" to "41650", "여주시" to "41670"
    )

    // [수정!] 전체 주소 문자열에서 직접 지역 코드를 찾는 더 안정적인 함수
    fun findCdFromAddress(fullAddress: String): String? {
        if (fullAddress.contains("서울특별시")) {
            // "서울특별시 종로구" -> "종로구"를 찾아서 코드를 반환
            seoulLawdCdMap.entries.find { fullAddress.contains(it.key) }?.let { return it.value }
        }
        if (fullAddress.contains("경기도")) {
            // "경기도 수원시 장안구" -> "수원시 장안구"를 찾아서 코드를 반환
            gyeonggiLawdCdMap.entries.find { fullAddress.contains(it.key) }?.let { return it.value }
        }
        return null
    }
}