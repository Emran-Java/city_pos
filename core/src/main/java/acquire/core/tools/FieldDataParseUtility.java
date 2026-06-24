package acquire.core.tools;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import acquire.base.utils.LoggerUtils;
import acquire.core.model.PayFlexField63ResponseModel;

public class FieldDataParseUtility {

    public static PayFlexField63ResponseModel parseField63ASci(String field63) {
        PayFlexField63ResponseModel details = new PayFlexField63ResponseModel();
        try {
            int[] lengths = {4, 2, 5, 9, 6, 9, 9, 9, 6, 7, 4};

            // Validate total length
            int totalLength = Arrays.stream(lengths).sum();
            if (field63 == null || field63.length() < totalLength) {
                LoggerUtils.e("Invalid Field63 length: " + (field63 == null ? 0 : field63.length()));
                return details;
            }

            int index = 0;
            String[] values = new String[lengths.length];

            // Extract all fields dynamically
            for (int i = 0; i < lengths.length; i++) {
                values[i] = field63.substring(index, index + lengths[i]);
                index += lengths[i];
            }

            /*
                30303132 		=> 0012     (4)
                3132			=> 12		(2)
                3030303030		=> 00000	(5)
                303030303030313233	=> 000000123	(9)
                303030303030		=> 000000	    (6)
                303030303030313233	=> 000000123	(9)
                303030303030303030	=> 000000000	(9)
                303030303030303030	=> 000000000	(9)
                303030303030		=> 000000	    (6)
                31313131313131	=> 1111111	(7)
                39393939		=> 9999 	(4)
            * */

            // Map values to model
            details.setProgramId(values[0]);
            details.setInstallmentPeriod(values[1]);
            details.setInterestRate(values[2]);
            details.setTotalInstallmentAmount(values[3]);
            details.setTotalInterestAmount(values[4]);
            details.setFirstInstallmentAmount(values[5]);
            details.setDownpaymentAmount(values[6]);
            details.setMonthlyInstallmentAmount(values[7]);
            details.setMonthlyInstallmentInterest(values[8]);
            details.setVendorId(values[9]);
            details.setProductId(values[10]);

            return details;
        } catch (Exception ex) {
            LoggerUtils.e("newCall Brac PayFlex-Sale FieldDataParseUtility.Exception:  " + ex.getMessage());
        }

        return details;
    }

    public static PayFlexField63ResponseModel parseField63(String field63) {

        AtomicInteger index = new AtomicInteger();
        PayFlexField63ResponseModel details = new PayFlexField63ResponseModel();

        try {
            // Helper to extract substring
            java.util.function.Function<Integer, String> next = (length) -> {
                String part = field63.substring(index.get(), index.get() + length);
                index.addAndGet(length);
                return part;
            };
            String programIdHex = next.apply(8);
            String installmentPeriodHex = next.apply(4);
            String interestRateHex = next.apply(10);
            String totalInstallmentAmountHex = next.apply(18);
            String totalInterestAmountHex = next.apply(12);
            String firstInstallmentAmountHex = next.apply(18);
            String downpaymentAmountHex = next.apply(18);
            String monthlyInstallmentAmountHex = next.apply(18);
            String monthlyInstallmentInterestHex = next.apply(12);
            String vendorIdHex = next.apply(14);
            String productIdHex = next.apply(8);

            details.setProgramId(hexToAscii(programIdHex));
            details.setInstallmentPeriod(hexToAscii(installmentPeriodHex));
            details.setInterestRate(hexToAscii(interestRateHex));
            details.setTotalInstallmentAmount(hexToAscii(totalInstallmentAmountHex));
            details.setTotalInterestAmount(hexToAscii(totalInterestAmountHex));
            details.setFirstInstallmentAmount(hexToAscii(firstInstallmentAmountHex));
            details.setDownpaymentAmount(hexToAscii(downpaymentAmountHex));
            details.setMonthlyInstallmentAmount(hexToAscii(monthlyInstallmentAmountHex));
            details.setMonthlyInstallmentInterest(hexToAscii(monthlyInstallmentInterestHex));
            details.setVendorId(hexToAscii(vendorIdHex));
            details.setProductId(hexToAscii(productIdHex));
        }catch (Exception ex){

            LoggerUtils.e("newCall Brac PayFlex-Sale FieldDataParseUtility.Exception:  "+ex.getMessage());
        }

        return details;
    }

    private static String hexToAscii(String hex) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}
