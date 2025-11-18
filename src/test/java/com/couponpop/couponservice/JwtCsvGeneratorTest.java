package com.couponpop.couponservice;

import com.couponpop.security.token.JwtProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 테스트용 JWT 토큰을 포함한 CSV 파일 생성기.
 * - JMeter 등 부하 테스트에서 회원별 JWT 토큰 데이터를 준비하기 위한 용도.
 */
@Disabled("로컬 전용 테스트 - CI에서는 실행하지 않음")
@SpringBootTest
public class JwtCsvGeneratorTest {

    @Autowired
    private JwtProvider jwtProvider;

    private static final int MEMBER_COUNT = 1000;
    private static final String FILE_NAME = "scripts/jmeter/coupon_issue.csv";

    /**
     * CSV 파일 생성
     * - 기존 파일이 존재하면 삭제 후 새로 생성
     * - 첫 줄: 헤더(memberId, jwt)
     * - 이후: 회원별 JWT 토큰 데이터
     */
    public void generateCsvFile() {
        File csv = new File(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csv, false))) { // false → 덮어쓰기 모드
            // 기존 파일이 있다면 삭제
            if (csv.exists() && csv.delete()) {
                System.out.println("기존 " + FILE_NAME + " 파일 삭제 완료");
            }

            // 헤더 작성
            bw.write("memberId,jwt");
            bw.newLine();

            // 회원별 JWT 생성 및 작성
            for (int i = 1; i <= MEMBER_COUNT; i++) {
                Long memberId = (long) i;
                String jwt = jwtProvider.createAccessToken(memberId, "user" + memberId + "@test.com", "CUSTOMER");
                bw.write(memberId + "," + jwt);
                bw.newLine();
            }

            System.out.println(FILE_NAME + " 생성 완료 (" + MEMBER_COUNT + "명)");
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 생성 중 오류 발생", e);
        }
    }

    @Test
    void createJwtCSV() {
        generateCsvFile();
    }
}
