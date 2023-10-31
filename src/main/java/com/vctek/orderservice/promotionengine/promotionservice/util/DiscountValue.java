package com.vctek.orderservice.promotionengine.promotionservice.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DiscountValue {
    private final String code;
    private final double value;
    private final double appliedValue;
    private final boolean absolute;
    private final String isoCode;
    private final boolean asTargetPrice;
    private static final String DV_HEADER = "<DV<";
    private static final String DV_FOOTER = ">VD>";
    private static final String INTERNAL_DELIMITER = "#";
    private static final String DELIMITER = "|";
    private static final String EMPTY = "[]";

    public static DiscountValue createRelative(String code, Double doubleValue) {
        return new DiscountValue(code, doubleValue, false, (String) null);
    }

    public static DiscountValue createAbsolute(String code, Double doubleValue, String currencyIsoCode) {
        return new DiscountValue(code, doubleValue, true, currencyIsoCode);
    }

    public static DiscountValue createTargetPrice(String code, Double doubleValue, String currencyIsoCode) {
        return new DiscountValue(code, doubleValue, currencyIsoCode, true);
    }

    public DiscountValue apply(double quantity, double price, int digits, String currencyIsoCode) {
        if (this.isAbsolute() && (currencyIsoCode == null || !currencyIsoCode.equals(this.getCurrencyIsoCode()))) {
            throw new IllegalArgumentException("cannot apply price " + price + " with currency " + currencyIsoCode + " to absolute discount with currency " + this.getCurrencyIsoCode());
        }
        if (this.isAbsolute()) {
            return this.isAsTargetPrice() ? this.createTargetPriceAppliedValue(quantity, price, digits, currencyIsoCode) : this.createAbsoluteAppliedValue(quantity, digits, currencyIsoCode);
        }
        return this.createRelativeAppliedValue(price, digits, currencyIsoCode);
    }

    protected DiscountValue createRelativeAppliedValue(double price, int digits, String currencyIsoCode) {
        return new DiscountValue(this.getCode(), this.getValue(), false, DiscountValue.round(price * this.getValue() / 100.0D, digits > 0 ? digits : 0), currencyIsoCode);
    }

    protected DiscountValue createAbsoluteAppliedValue(double quantity, int digits, String currencyIsoCode) {
        return new DiscountValue(this.getCode(), this.getValue(), true, DiscountValue.round(this.getValue() * quantity, digits > 0 ? digits : 0), currencyIsoCode);
    }

    protected DiscountValue createTargetPriceAppliedValue(double quantity, double totalPriceWithoutDiscounts, int digits, String currencyIsoCode) {
        double targetPricePerPiece = this.getValue();
        double totalTargetPrice = targetPricePerPiece * quantity;
        double differenceToTargetPrice = totalPriceWithoutDiscounts - totalTargetPrice;
        return new DiscountValue(this.getCode(), this.getValue(), true, DiscountValue.round(differenceToTargetPrice, digits > 0 ? digits : 0), currencyIsoCode, true);
    }

    public static List apply(double quantity, double startPrice, int digits, List values, String currencyIsoCode) {
        List ret = new ArrayList(values.size());
        double tmp = startPrice;
        Iterator it = values.iterator();

        while (it.hasNext()) {
            DiscountValue discountValue = ((DiscountValue) it.next()).apply(quantity, tmp, digits, currencyIsoCode);
            tmp -= discountValue.getAppliedValue();
            ret.add(discountValue);
        }

        return ret;
    }

    public static double sumAppliedValues(Collection values) {
        if (values != null && !values.isEmpty()) {
            double sum = 0.0D;

            for (Iterator it = values.iterator(); it.hasNext(); sum += ((DiscountValue) it.next()).getAppliedValue()) {
                ;
            }

            return sum;
        } else {
            return 0.0D;
        }
    }

    public String getCode() {
        return this.code;
    }

    public String getCurrencyIsoCode() {
        return this.isoCode;
    }

    public double getValue() {
        return this.value;
    }

    public boolean isAsTargetPrice() {
        return this.asTargetPrice;
    }

    public double getAppliedValue() {
        return this.appliedValue;
    }

    public boolean isAbsolute() {
        return this.absolute;
    }

    public DiscountValue(String code, double value, boolean absolute, String currencyIsoCode) {
        this(code, value, absolute, 0.0D, currencyIsoCode);
    }

    public DiscountValue(String code, double value, String currencyIsoCode, boolean asTargetPrice) {
        this(code, value, true, 0.0D, currencyIsoCode, asTargetPrice);
    }

    public DiscountValue(String code, double value, boolean absolute, double appliedValue, String isoCode) {
        this(code, value, absolute, appliedValue, isoCode, false);
    }

    public DiscountValue(String code, double value, boolean absolute, double appliedValue, String isoCode, boolean asTargetPrice) {
        if (code == null) {
            throw new IllegalArgumentException("discount value code may not be null");
        } else if (asTargetPrice && StringUtils.isBlank(isoCode)) {
            throw new IllegalArgumentException("discount value cannot be target price without currency iso code");
        } else {
            this.code = code;
            this.value = value;
            this.absolute = absolute;
            this.appliedValue = appliedValue;
            this.isoCode = isoCode;
            this.asTargetPrice = asTargetPrice;
        }
    }

    public DiscountValue cloneDiscountValue() {
        return new DiscountValue(this.getCode(), this.getValue(), this.isAbsolute(), this.getAppliedValue(), this.getCurrencyIsoCode());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(DV_HEADER);
        sb.append(this.getCode());
        sb.append(INTERNAL_DELIMITER).append(this.getValue());
        sb.append(INTERNAL_DELIMITER).append(this.isAbsolute());
        sb.append(INTERNAL_DELIMITER).append(this.getAppliedValue());
        sb.append(INTERNAL_DELIMITER).append(this.getCurrencyIsoCode() != null ? this.getCurrencyIsoCode() : "NULL");
        sb.append(INTERNAL_DELIMITER).append(this.isAsTargetPrice());
        sb.append(DV_FOOTER);
        return sb.toString();
    }

    public static String toString(Collection discountValueCollection) {
        if (discountValueCollection == null) {
            return null;
        }
        if (discountValueCollection.isEmpty()) {
            return EMPTY;
        }

        StringBuilder stringBuilder = new StringBuilder("[");
        Iterator it = discountValueCollection.iterator();

        while (it.hasNext()) {
            stringBuilder.append(it.next().toString());
            if (it.hasNext()) {
                stringBuilder.append(DELIMITER);
            }
        }

        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public static Collection parseDiscountValueCollection(String str) throws IllegalArgumentException {
        if (StringUtils.isBlank(str)) {
            return null;
        }

        if (str.equals(EMPTY)) {
            return new LinkedList();
        }

        Collection ret = new LinkedList();
        StringTokenizer st = new StringTokenizer(str.substring(1, str.length() - 1), DELIMITER);

        while (st.hasMoreTokens()) {
            ret.add(parseDiscountValue(st.nextToken()));
        }

        return ret;
    }

    public static DiscountValue parseDiscountValue(String str) throws IllegalArgumentException {
        int start = str.indexOf(DV_HEADER);
        if (start < 0) {
            throw new IllegalArgumentException("could not find <DV< in discount value string '" + str + "'");
        }

        int end = str.indexOf(DV_FOOTER, start);
        if (start < 0) {
            throw new IllegalArgumentException("could not find >VD> in discount value string '" + str + "'");
        }

        int pos = 0;
        String code = null;
        double value = 0.0D;
        boolean absolute = false;
        boolean asTargetPrice = false;
        double appliedValue = 0.0D;
        String iso = null;
        String substr = str.substring(start + DV_HEADER.length(), end);
        if (substr.startsWith(INTERNAL_DELIMITER)) {
            pos = 1;
            code = "";
        }

        for (StringTokenizer st = new StringTokenizer(substr, INTERNAL_DELIMITER); st.hasMoreTokens(); ++pos) {
            String token = st.nextToken();
            switch (pos) {
                case 0:
                    code = token;
                    break;
                case 1:
                    value = Double.parseDouble(token);
                    break;
                case 2:
                    absolute = Boolean.parseBoolean(token);
                    break;
                case 3:
                    appliedValue = Double.parseDouble(token);
                    break;
                case 4:
                    iso = token;
                    break;
                case 5:
                    asTargetPrice = Boolean.parseBoolean(token);
                    break;
                default:
                    throw new IllegalArgumentException("illegal discount value string '" + str + "' (pos=" + pos + ", moreTokens=" + st.hasMoreTokens() + ", nextToken='" + token + "')");
            }
        }

        if (pos < 3) {
            throw new IllegalArgumentException("illegal discount value string '" + str + "' (pos=" + pos + ")");
        }

        return new DiscountValue(code, value, absolute, appliedValue, "NULL".equals(iso) ? null : iso, asTargetPrice);
    }

    public int hashCode() {
        return this.isAbsolute() ? this.getCode().hashCode() ^ (this.isAbsolute() ? 1 : 0) ^ (int) this.getValue() ^ (this.getCurrencyIsoCode() == null ? 0 : this.getCurrencyIsoCode().hashCode()) : this.getCode().hashCode() ^ (this.isAbsolute() ? 1 : 0) ^ (int) this.getValue();
    }

    public boolean equals(Object object) {
        return object instanceof DiscountValue &&
                this.getCode().equals(((DiscountValue) object).getCode())
                && this.absolute == ((DiscountValue) object).isAbsolute()
                && (!this.absolute || this.isoCode == null &&
                ((DiscountValue) object).getCurrencyIsoCode() == null ||
                ((DiscountValue) object).getCurrencyIsoCode().equals(this.isoCode))
                && this.value == ((DiscountValue) object).getValue()
                && this.appliedValue == ((DiscountValue) object).getAppliedValue();
    }

    public boolean equalsIgnoreAppliedValue(DiscountValue discountValue) {
        String iso = this.isoCode;
        return this.getCode().equals(discountValue.getCode())
                && this.absolute == discountValue.isAbsolute()
                && (!this.absolute || iso == null && discountValue.getCurrencyIsoCode() == null
                || discountValue.getCurrencyIsoCode().equals(iso))
                && this.value == discountValue.getValue();
    }

    public static double round(double value, int digits) {
        if (Double.isFinite(value)) {
            BigDecimal bdValue = new BigDecimal(Double.toString(value));
            return bdValue.setScale(digits, RoundingMode.HALF_UP).doubleValue();
        } else {
            return value;
        }
    }
}
