package acquire.base.utils.emv;

/**
 * Emv  Tag
 *
 * @author Janson
 * @date 2022/7/18 10:55
 */
public class EmvTag {

    /*
     * IC Card Data
     */
    /**
     * 9F26 - Application Cryptogram
     */
    public static final int TAG_9F26_IC_AC = 0x9F26;
    /**
     * 9F42 - Application Currency Code
     */
    public static final int TAG_9F42_IC_APPCURCODE = 0x9F42;
    /**
     * 9F44 - Application Currency Exponent
     */
    public static final int TAG_9F44_IC_APPCUREXP = 0x9F44;
    /**
     * 9F05 - Application Discretionary Data
     */
    public static final int TAG_9F05_IC_APPDISCDATA = 0x9F05;
    /**
     * 5F25 - Application Effective Date
     */
    public static final int TAG_5F25_IC_APPEFFECTDATE = 0x5F25;
    /**
     * 5F24 - Application Expiration Date
     */
    public static final int TAG_5F24_IC_APPEXPIREDATE = 0x5F24;
    /**
     * 94 - Application File Locator
     */
    public static final int TAG_94_IC_AFL = 0x94;
    /**
     * 4F - Application Identifier
     */
    public static final int TAG_4F_IC_AID = 0x4F;
    /**
     * 82 - Application Interchange Profile
     */
    public static final int TAG_82_IC_AIP = 0x82;
    /**
     * 50 - Application Label
     */
    public static final int TAG_50_IC_APPLABEL = 0x50;
    /**
     * 9F12 - Application Preferred Name
     */
    public static final int TAG_9F12_IC_APPNAME = 0x9F12;
    /**
     * 5A - Application Primary Account Number
     */
    public static final int TAG_5A_IC_PAN = 0x5A;
    /**
     * 5F34 - Application Primary Account Number Sequence Number
     */
    public static final int TAG_5F34_IC_PANSN = 0x5F34;
    /**
     * 87 - Application Priority Indicator
     */
    public static final int TAG_87_IC_APID = 0x87;
    /**
     * 9F3B - Application Preference Currency
     */
    public static final int TAG_9F3B_IC_APCUR = 0x9F3B;
    /**
     * 9F43 - Application Preferece Currency Exponent
     */
    public static final int TAG_9F43_IC_APCUREXP = 0x9F43;
    /**
     * 61 - Application Template
     */
    public static final int TAG_61_IC_APPTEMP = 0x61;
    /**
     * 9F36 - Application Transaction Counter
     */
    public static final int TAG_9F36_IC_ATC = 0x9F36;
    /**
     * 9F07 - Application Usage Control
     */
    public static final int TAG_9F07_IC_AUC = 0x9F07;
    /**
     * 9F08 - Application Version Number
     */
    public static final int TAG_9F08_IC_APPVERNO = 0x9F08;
    /**
     * 8C - Card Risk Management Data Object List 1
     */
    public static final int TAG_8C_IC_CDOL1 = 0x8C;
    /**
     * 8D - Card Risk Management Data Object List 2
     */
    public static final int TAG_8D_IC_CDOL2 = 0x8D;
    /**
     * 5F20 - Cardholder Name
     */
    public static final int TAG_5F20_IC_HOLDERNAME = 0x5F20;
    /**
     * 9F0B - Cardholder Name Extended
     */
    public static final int TAG_9F0B_IC_HOLDERNAMEEX = 0x9F0B;
    /**
     * 8E - Cardholder Verification Method List
     */
    public static final int TAG_8E_IC_CVMLIST = 0x8E;
    /**
     * 8F - Certification Authority Public Key Index
     */
    public static final int TAG_8F_IC_CAPKINDEX = 0x8F;
    /**
     * 9F27 - Cryptogram Information Data
     */
    public static final int TAG_9F27_IC_CID = 0x9F27;
    /**
     * 9F45 - Data Authentication Code
     */
    public static final int TAG_9F45_IC_DTAUTHCODE = 0x9F45;

    /**
     * 9F6E - Third Party Data / Form Factor Indicator
     */
    public static final int TAG_9F6E_FF_INDICATOR = 0x9F6E;
    /**
     * 84 - Dedicated File Name
     */
    public static final int TAG_84_IC_DFNAME = 0x84;
    /**
     * 9D - Directory Definition File
     */
    public static final int TAG_9D_IC_DDFNAME = 0x9D;
    /**
     * 73 - Directory Discretionary Template
     */
    public static final int TAG_73_IC_DIRDISCTEMP = 0x73;
    /**
     * 9F49 - Dynamic Data Authentication Data Object List
     */
    public static final int TAG_9F49_IC_DDOL = 0x9F49;
    /**
     * BF0C - File Control Information Issuer Discretionary Data
     */
    public static final int TAG_BF0C_IC_FCIDISCDATA = 0xBF0C;
    /**
     * A5 - File Control Information Proprietary Template
     */
    public static final int TAG_A5_IC_FCIPROPTEMP = 0xA5;
    /**
     * 6F - File Control Information Template
     */
    public static final int TAG_6F_IC_FCITEMP = 0x6F;
    /**
     * 9F4C - ICC Dynamic Number
     */
    public static final int TAG_9F4C_IC_ICCDYNNUM = 0x9F4C;
    /**
     * 9F2D - ICC PIN Encipherment Public Key Certificate
     */
    public static final int TAG_9F2D_IC_PECERT = 0x9F2D;
    /**
     * 9F2E - ICC PIN Encipherment Public Key Exponent
     */
    public static final int TAG_9F2E_IC_PEEXP = 0x9F2E;
    /**
     * 9F2F - ICC PIN Encipherment Public Key Remainder
     */
    public static final int TAG_9F2F_IC_PERMD = 0x9F2F;
    /**
     * 9F46 - ICC Public Key Certificate
     */
    public static final int TAG_9F46_IC_ICCPKCERT = 0x9F46;
    /**
     * 9F47 - ICC Public Key Exponent
     */
    public static final int TAG_9F47_IC_ICCPKEXP = 0x9F47;
    /**
     * 9F48 - ICC Public Key Remainder
     */
    public static final int TAG_9F48_IC_ICCPKRMD = 0x9F48;
    /**
     * 9F0D - Issuer Action Code-Default
     */
    public static final int TAG_9F0D_IC_IAC_DEFAULT = 0x9F0D;
    /**
     * 9F0E - Issuer Action Code-Denial
     */
    public static final int TAG_9F0E_IC_IAC_DENIAL = 0x9F0E;
    /**
     * 9F0F - Issuer Action Code-Online
     */
    public static final int TAG_9F0F_IC_IAC_ONLINE = 0x9F0F;
    /**
     * 9F10 - Issuer Application Data
     */
    public static final int TAG_9F10_IC_ISSAPPDATA = 0x9F10;
    /**
     * 9F11 - Issuer Code Table Index
     */
    public static final int TAG_9F11_IC_ISSCTINDEX = 0x9F11;
    /**
     * 5F28 - Issuer Country Code
     */
    public static final int TAG_5F28_IC_ISSCOUNTRYCODE = 0x5F28;
    /**
     * 90 - Issuer Public Key Certificate
     */
    public static final int TAG_90_IC_ISSPKCERT = 0x90;
    /**
     * 9F32 - Issuer Public Key Exponent
     */
    public static final int TAG_9F32_IC_ISSPKEXP = 0x9F32;
    /**
     * 92 - Issuer Public Key Remainder
     */
    public static final int TAG_92_IC_ISSPKRMD = 0x92;
    /**
     * 5F50 - Issuer URL
     */
    public static final int TAG_5F50_IC_ISSURL = 0x5F50;
    /**
     * 5F2D - Language Preferenc
     */
    public static final int TAG_5F2D_IC_LANGPREF = 0x5F2D;
    /**
     * 9F13 - Last Online Application Transaction Counter Register
     */
    public static final int TAG_9F13_IC_LASTATC = 0x9F13;
    /**
     * 9F14 - Lower Consecutive Offline Limit
     */
    public static final int TAG_9F14_IC_LCOL = 0x9F14;
    /**
     * 9F17 - Personal Identification Number Try Counter
     */
    public static final int TAG_9F17_IC_PINTRYCNTR = 0x9F17;
    /**
     * 9F38 - Processing Options Data Object List
     */
    public static final int TAG_9F38_IC_PDOL = 0x9F38;
    /**
     * 80 - Response Message Template Format 1
     */
    public static final int TAG_80_IC_RMTF1 = 0x80;
    /**
     * 77 - Response Message Template Format 2
     */
    public static final int TAG_77_IC_RMTF2 = 0x77;
    /**
     * 5F30 - Service Code
     */
    public static final int TAG_5F30_IC_SERVICECODE = 0x5F30;
    /**
     * 88 - Short File Indicator
     */
    public static final int TAG_88_IC_SFI = 0x88;
    /**
     * 9F4B - Signed Dynamic Application Data
     */
    public static final int TAG_9F4B_IC_SIGNDYNAPPDT = 0x9F4B;
    /**
     * 93 - Signed Static Application Data
     */
    public static final int TAG_93_IC_SIGNSTAAPPDT = 0x93;
    /**
     * 9F4A - Static Data Authentication Tag List
     */
    public static final int TAG_9F4A_IC_SDATAGLIST = 0x9F4A;
    /**
     * 9F1F - Track 1 Discretionary Data
     */
    public static final int TAG_9F1F_IC_TRACK1DATA = 0x9F1F;
    /**
     * 9F20 - Track 2 Discretionary Data
     */
    public static final int TAG_9F20_IC_TRACK2DATA = 0x9F20;
    /**
     * 57 - Track 2 Equivalent Data
     */
    public static final int TAG_57_IC_TRACK2EQUDATA = 0x57;
    /**
     * 97 - Transaction Certificate Data Object List
     */
    public static final int TAG_97_IC_TDOL = 0x97;
    /**
     * 9F23 - Upper Consecutive Offline Limit
     */
    public static final int TAG_9F23_IC_UCOL = 0x9F23;
    /**
     * DF31 - Issuer Script Results
     */
    public static final int TAG_DF31_IC_IISSCRIRES = 0xDF31;
    /**
     * 9F63- Card Product Idatification
     */
    public static final int TAG_9F63_IC_PRODUCTID = 0x9F63;

    /*
     * Terminal data
     */
    /**
     * 9F01 - Acquirer Identifier
     */
    public static final int TAG_9F01_TM_ACQID = 0x9F01;
    /**
     * 9F40 - Additional Terminal Capability
     */
    public static final int TAG_9F40_TM_CAP_AD = 0x9F40;
    /**
     * 81 - Amount,Authorised(Binary)
     */
    public static final int TAG_81_TM_AUTHAMNTB = 0x81;
    /**
     * 9F02 - Amount,Authorised(Binary)
     */
    public static final int TAG_9F02_TM_AUTHAMNTN = 0x9F02;
    /**
     * 9F04 - Amount,Other(Binary)
     */
    public static final int TAG_9F04_TM_OTHERAMNTB = 0x9F04;
    /**
     * 9F03 - Amount,Other(Numeric)
     */
    public static final int TAG_9F03_TM_OTHERAMNTN = 0x9F03;
    /**
     * 9F3A - Amount,Reference Currency
     */
    public static final int TAG_9F3A_TM_REFCURAMNT = 0x9F3A;
    /**
     * 9F06 - Terminal Application Identifier
     */
    public static final int TAG_9F06_TM_AID = 0x9F06;
    /**
     * 9F09 - Terminal Application Version Number
     */
    public static final int TAG_9F09_TM_APPVERNO = 0x9F09;
    /**
     * 89 - Authorization Code
     */
    public static final int TAG_89_TM_AUTHCODE = 0x89;
    /**
     * 8A - Authorisation Response Code
     */
    public static final int TAG_8A_TM_ARC = 0x8A;
    /**
     * 9F34 - Cardholder Verification Method Results
     */
    public static final int TAG_9F34_TM_CVMRESULT = 0x9F34;
    /**
     * 9F22 - Terminal Certification Authority Public Key Index
     */
    public static final int TAG_9F22_TM_CAPKINDEX = 0x9F22;
    /**
     * 9F1E - Interface Device Serial Number
     */
    public static final int TAG_9F1E_TM_IFDSN = 0x9F1E;
    /**
     * 91 - Issuer Authentication Data
     */
    public static final int TAG_91_TM_ISSAUTHDT = 0x91;
    /**
     * 86 - Issuer Script Command
     */
    public static final int TAG_86_TM_ISSSCRCMD = 0x86;
    /**
     * 9F18 - Issuer Script Identifier
     */
    public static final int TAG_9F18_TM_ISSSCRID = 0x9F18;
    /**
     * 9F15 - Merchant Category Code
     */
    public static final int TAG_9F15_TM_MCHCATCODE = 0x9F15;
    /**
     * 9F16 - Merchant Identifier
     */
    public static final int TAG_9F16_TM_MCHID = 0x9F16;
    /**
     * 9F39 - Point-of-Service Entry Mode
     */
    public static final int TAG_9F39_TM_POSENTMODE = 0x9F39;
    /**
     * 9F33 - Terminal Capabilities
     */
    public static final int TAG_9F33_TM_CAP = 0x9F33;
    /**
     * 9F1A - Terminal Country Code
     */
    public static final int TAG_9F1A_COUNTRY_CODE = 0x9F1A;
    /**
     * 9F1B - Terminal Floor Limit
     */
    public static final int TAG_9F1B_TM_FLOORLMT = 0x9F1B;
    /**
     * 9F1C - Terminal Identification
     */
    public static final int TAG_9F1C_TM_TERMID = 0x9F1C;
    /**
     * 9F1D - Terminal Risk Management Data
     */
    public static final int TAG_9F1D_TM_RMDATA = 0x9F1D;
    /**
     * 9F35 - Terminal Type
     */
    public static final int TAG_9F35_TM_TERMTYPE = 0x9F35;
    /**
     * 95 - Terminal Verification Result
     */
    public static final int TAG_95_TM_TVR = 0x95;
    /**
     * 98 - Transaction Certificate Hash Value
     */
    public static final int TAG_98_TM_TCHASH = 0x98;
    /**
     * 5F2A - Transaction Currency Code
     */
    public static final int TAG_5F2A_CURRENCY_CODE = 0x5F2A;
    /**
     * 5F36 - Transaction Currency Exponent
     */
    public static final int TAG_5F36_CURRENCY_EXP = 0x5F36;
    /**
     * 9A - Transaction Date
     */
    public static final int TAG_9A_TM_TRANSDATE = 0x9A;
    /**
     * 99 - Transaction Personal Identification Number Data
     */
    public static final int TAG_99_TM_PINDATA = 0x99;
    /**
     * 9F3C - Transaction Reference Currency Code
     */
    public static final int TAG_9F3C_TM_REFCURCODE = 0x9F3C;
    /**
     * 9F3D - Transaction Reference Currency Exponent
     */
    public static final int TAG_9F3D_TM_REFCUREXP = 0x9F3D;
    /**
     * 9F41 - Transaction Sequence Counter
     */
    public static final int TAG_9F41_TM_TRSEQCNTR = 0x9F41;
    /**
     * 9B - Transaction Status Information
     */
    public static final int TAG_9B_TM_TSI = 0x9B;
    /**
     * 9F21 - Transaction Time
     */
    public static final int TAG_9F21_TM_TRANSTIME = 0x9F21;
    /**
     * 9C - Transaction Type
     */
    public static final int TAG_9C_TM_TRANSTYPE = 0x9C;
    /**
     * 9F37 - Unpredictable Number
     */
    public static final int TAG_9F37_TM_UNPNUM = 0x9F37;

    /**
     * 71 - Issuer Script Template 1
     */
    public static final int TAG_71_ISSSCR_TEMPLATE_1 = 0x71;
    /**
     * 72 - Issuer Script Template 2
     */
    public static final int TAG_72_ISSSCR_TEMPLATE_2 = 0x72;

    /**
     * 9F7A - EC Terminal Support Indicator
     */
    public static final int TAG_9F7A_EC_TM_INDI = 0x9F7A;
    /**
     * 9F7B - Reader Contactless Transaction Limit
     */
    public static final int TAG_9F7B_CONTACTLESS_LIMIT = 0x9F7B;
    /**
     * DF01 - Application Selection Indicator
     */
    public static final int TAG_DF01_ASI = 0xDF01;
    /**
     * DF11 - TAC Default
     */
    public static final int TAG_DF11_TAC_DEFAULT = 0xDF11;
    /**
     * DF12 - TAC Online
     */
    public static final int TAG_DF12_TAC_ONLINE = 0xDF12;
    /**
     * DF13 - TAC Denial
     */
    public static final int TAG_DF13_TAC_DENIAL = 0xDF13;

    /**
     * DF15 - Threshold Value for Biased Random Selection
     */
    public static final int TAG_DF15_BRD_THRESHOLD_VALUE = 0xDF15;
    /**
     * DF16 - Maximum Target Percentage to be used for Biased Random Selection
     */
    public static final int TAG_DF16_BRD_MAX_PERCENT = 0xDF16;
    /**
     * DF17 - Target Percentage to be Used for Random Selection
     */
    public static final int TAG_DF17_RS_PERCENT = 0xDF17;
    /**
     * DF19 - Contactless FLOOR LIMIT (If the amount exceeds it, offline -> online)
     */
    public static final int TAG_DF19_CONTACTLESS_FLOOR_LIMIT = 0xDF19;
    /**
     * DF20 - Contactless Transaction Limit.(If the amount exceeds it, denial)
     */
    public static final int TAG_DF20_CONTACTLESS_LIMIT = 0xDF20;
    /**
     * DF21 - CVM Required Limit.(If the amount exceeds it, query CVM list )
     */
    public static final int TAG_DF21_CONTACTLESS_CVM_LIMIT = 0xDF21;
    /**
     * DF24 - ICS
     */
    public static final int TAG_DF24_ICS = 0xDF24;
    /**
     * 9F58 - interact Merchant Type Indicator
     */
    public static final int TAG_9F58_IMTI = 0x9F58;
    /**
     * 9F5E - interact Terminal Option Status
     */
    public static final int TAG_9F5E_ITS = 0x9F5E;
    /**
     * 9F59 - interact Terminal Transaction Information
     */
    public static final int TAG_9F59_ITTI = 0x9F59;
    /**
     * DF37 - Kernel ID
     */
    public static final int TAG_DF37_KERNEL_ID = 0xDF37;
    /**
     * DF52 - TLV Buffer
     */
    public static final int TAG_DF52_TLV_BUFFER = 0xDF52;
    /**
     * 9F53 - Terminal Interchange Profile (static/dynamic)
     */
    public static final int TAG_9F53_TIP = 0x9F53;
    /**
     * DF60 - Combination Options
     */
    public static final int TAG_DF60_COMBIN = 0xDF60;
    /**
     * 9F66 - Terminal Transaction Qualifiers
     */
    public static final int TAG_9F66_TTQ = 0x9F66;
    /**
     * DF4A - Unpredictable Number Range
     */
    public static final int TAG_DF4A_UN_RANGE = 0xDF4A;
    /**
     * DF48 - CVM Capability
     */
    public static final int TAG_DF48_CVM_CAP = 0xDF48;
    /**
     * DF42 - Mag-stripe CVM Capability – CVM Required-'DF811E'
     */
    public static final int TAG_DF42_MAG_CVM_CAP = 0xDF42;
    /**
     * DF47 - Mag-stripe CVM Capability – No CVM Required-'DF812C'
     */
    public static final int TAG_DF47_MAG_CVM_CAP_NO_REQ = 0xDF47;
    /**
     * DF2F - Kernel Configuration- ‘DF811B’
     */
    public static final int TAG_DF2F_KERNEL_CONFIG = 0xDF2F;
    /**
     * DF54 - Maximum Relay Resistance Grace Period- 'DF8133’
     */
    public static final int TAG_DF54_MAX_RRGP = 0xDF54;
    /**
     * DF55 - Minimum Relay Resistance Grace Period- 'DF8132'
     */
    public static final int TAG_DF55_MIN_RRGP = 0xDF55;
    /**
     * DF56 - Relay Resistance Accuracy Threshold- 'DF8136'
     */
    public static final int TAG_DF56_RRA_THRESHOLD = 0xDF56;
    /**
     * DF57 - Relay Resistance Transmission Time Mismatch Threshold- 'DF8137’
     */
    public static final int TAG_DF57_RRTTM_THRESHOLD = 0xDF57;
    /**
     * DF58 - Terminal Expected Transmission Time For Relay Resistance C-APDU- ‘DF8134’
     */
    public static final int TAG_DF58_CAPDU_TIME = 0xDF58;
    /**
     * DF59 - Terminal Expected Transmission Time For Relay Resistance R-APDU- 'DF8135’
     */
    public static final int TAG_DF59_RAPDU_TIME = 0xDF59;
    /**
     * DF2B - Default UDOL- 'DF811A'
     */
    public static final int TAG_DF2B_DEFAULT_UDOL = 0xDF2B;
    /**
     * DF46 - Mobile Support Indicator-'9F7E’
     */
    public static final int TAG_DF46_MOBILE_SI = 0xDF46;
    /**
     * DF44 - DDOL
     */
    public static final int TAG_DF44_DDOL = 0xDF44;
    /**
     * DF45 - TDOL
     */
    public static final int TAG_DF45_TDOL = 0xDF45;
    /**
     * 1F8102 - Select by AID supported
     */
    public static final int TAG_1F8102_SELECT_BY_AID = 0x1F8102;
    /**
     * DF25 - EMV SELECT KERNEL
     */
    public static final int TAG_DF25_SELECT_KERNEL = 0xDF25;
    /**
     * DF7D - Transaction type.
     * the value of 9C (00/01/09/20)
     * when 1F8101==0x01, only this item is equal to the value of current transaction type (0x9C) then we select this aid.
     */
    public static final int TAG_DF7D_TRANS_TYPE = 0xDF7D;
}
