package Protocol;

public class LoadCode {

    public static Encoder getEncoder(){
        return new Encoder();
    }

    public static Decoder getDecoder(){
        return new Decoder();
    }

    public static class Encoder{
        private int nbitLength;

        public byte[] encode(byte[] inputData){
            byte[] res = split(inputData);

            return res;
        }

        private byte[] split(byte[] src){
            int binLength = src.length * 8;
            int remainder = binLength % 7;
            nbitLength = remainder == 0 ? binLength + 7 : (binLength/7+2) * 7;

            int[] binaryArray = new int[nbitLength];

            int length = 0;
            for (byte b : src) {
                for (int r = 7; r >= 0; r--) {
                    binaryArray[length] = b >> r & 1;
                    length++;
                }
            }

            if(remainder != 0){
                for(int i = 0; i < 7 - binLength % 7; i++){
                    binaryArray[nbitLength-7 + i] = 1;
                }
            }

            //System.out.println(Arrays.toString(binaryArray));
            return oddParity(binaryArray);
        }

        /**
         * 奇偶校验，并LSB
         * @param src
         * @return
         */
        private byte[] oddParity(int[] src){
            byte[] nbitByte = new byte[nbitLength / 7];
            for(int i = 0; i < nbitLength / 7 ; i++){
                byte res = 0;
                int odd = 0;
                for(int r = 6 ; r >= 0; r--){
                    int eachBit = src[i*7+r];
                    res += eachBit << (r+1);
                    if(eachBit == 1){
                        odd++;
                    }
                }
                res += (odd + 1) % 2;
                //System.out.println(Integer.toBinaryString(res));
                nbitByte[i] = res;
            }
            return nbitByte;
        }

    }

    public static class Decoder{
        private int[] toReform;
        private byte[] res;

        public byte[] decode(byte[] inputData){
            toReform = deOddParity(inputData);
            res = reform(toReform);

            return res;
        }

        private byte[] reform(int[] src){
            int lastByteSpare = 0;
            int[] originBinary;
            byte[] originBytes;
            for(int i = src.length-7; i < src.length ; i++){
                if(src[i] == 1){
                    lastByteSpare++;
                }
            }

            originBinary = new int[src.length - 7 - lastByteSpare];
            System.arraycopy(src, 0, originBinary, 0, originBinary.length);

            originBytes = new byte[originBinary.length/8];

            for(int i = 0; i < originBinary.length/8; i++){
                int res = 0;
                for(int r = 7 ; r >= 0; r--){
                    int eachBit = src[i*8+(7-r)];
                    res += eachBit << r;
                }
                //System.out.println(res);
                originBytes[i] = (byte) res;
            }
            //System.out.println(new String(originBytes));

            return originBytes;
        }

        private int[] deOddParity(byte[] src){

            int binLength = src.length * 7;

            int[] binaryArray = new int[binLength];

            int length = 0;
            for (byte b : src) {
                for (int r = 1; r <= 7; r++) {
                    binaryArray[length] = b >> r & 1;
                    length++;
                }
            }
            //System.out.println(Arrays.toString(binaryArray));
            return binaryArray;
        }

    }
}
