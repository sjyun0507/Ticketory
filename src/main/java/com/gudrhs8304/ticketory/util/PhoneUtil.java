// src/main/java/com/gudrhs8304/ticketory/util/PhoneUtil.java
package com.gudrhs8304.ticketory.util;

public final class PhoneUtil {
    private PhoneUtil() {}

    /** DB에 숫자만(예: 01012345678) 저장된 값을 010-1234-5678 형태로 변환 */
    public static String format(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {                  // 010-1234-5678
            return digits.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (digits.length() == 10) {           // 02, 031 등 지역번호 케이스 필요시
            return digits.replaceFirst("(\\d{2,3})(\\d{3,4})(\\d{4})", "$1-$2-$3");
        }
        return phone; // 길이가 예외적이면 원문 반환
    }

    /** 입력값을 DB용 숫자만으로 정규화 */
    public static String normalize(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\D", "");
    }
}