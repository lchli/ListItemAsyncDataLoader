package com.lchli.loaderlibrary.example.phoneInfoList;

/**
 * Created by lchli on 2016/4/24.
 */
public class PhoneInfoResponse {

    public int errNum;
    public String errMsg;
    public PhoneInfo retData;

    public static class PhoneInfo {
        public String telString;
        public String province;
        public String carrier;

        public int memorySize() {
            int size = 0;
            if (telString != null) {
                size += telString.getBytes().length;
            }
            if (province != null) {
                size += province.getBytes().length;
            }
            if (carrier != null) {
                size += carrier.getBytes().length;
            }
            return size;
        }

    }
}
