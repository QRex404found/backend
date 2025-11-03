// src/main/java/com/found/qrex/service/GeoLocationService.java

// src/main/java/com/found/qrex/service/GeoLocationService.java

package com.found.qrex.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.stereotype.Service;
import java.io.File;
import java.net.InetAddress; // <-- 임포트 확인

@Service
public class GeoLocationService {

    private final DatabaseReader dbReader;

    public GeoLocationService() throws Exception {
        // GeoLite2-City.mmdb 파일 경로 확인
        File database = new File("src/main/resources/GeoLite2-City.mmdb");
        this.dbReader = new DatabaseReader.Builder(database).build();
    }

    /**
     * URL의 IP 주소를 조회하고, 해당 IP의 국가/도시 위치를 반환합니다.
     */
    public String getIpLocation(String url) {
        // 🌟 [복구] try-catch 블록을 복구하여 실제 GeoIP 조회를 수행합니다.
        try {
            // 1. DNS Lookup (IP 주소 조회)
            String domain = extractDomain(url);
            InetAddress ipAddress = InetAddress.getByName(domain);

            // 2. GeoIP 조회
            CityResponse response = dbReader.city(ipAddress);
            String country = response.getCountry().getName();

            // RAG 프롬프트에 사용할 형식으로 반환
            return country;
        } catch (Exception e) {
            System.err.println("GeoIP/DNS 조회 오류: " + e.getMessage());
            return "Unknown"; // 오류 발생 시 Unknown 반환
        }
    }

    // 간단한 도메인 추출 로직 (유지)
    private String extractDomain(String url) {
        if (url.startsWith("http://")) url = url.substring(7);
        if (url.startsWith("https://")) url = url.substring(8);
        if (url.contains("/")) url = url.substring(0, url.indexOf("/"));
        return url;
    }

    // RAG 호출을 위해 IP 주소를 별도로 얻는 메소드 (선택적)
    public String getIpAddress(String url) {
        // 🌟 [복구] try-catch 블록을 복구하여 실제 DNS 조회를 수행합니다.
        try {
            String domain = extractDomain(url);
            InetAddress ipAddress = InetAddress.getByName(domain);
            return ipAddress.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1"; // 조회 실패 시 로컬 IP 반환
        }
    }
}