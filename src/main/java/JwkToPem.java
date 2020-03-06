//import com.chilkatsoft.CkJsonObject;
//import com.chilkatsoft.CkPublicKey;
//
//public class JwkToPem {
//    private static JwkToPem jwkToPem;
//    static {
//        try {
//            System.loadLibrary("chilkat");
//        } catch (UnsatisfiedLinkError e) {
//            System.err.println("Native code library failed to load.\n" + e);
//            System.exit(1);
//        }
//    }
//
//    public static JwkToPem getInstance(){
//        if (jwkToPem == null){
//            jwkToPem = new JwkToPem();
//        }
//        return jwkToPem;
//    }
//
//    public String main(String kty, String n, String e)
//    {
//        //  Note: This example requires Chilkat v9.5.0.66 or later.
//        boolean success;
//
//        //  First build a JWK sample to load..
//        CkJsonObject json = new CkJsonObject();
//        json.UpdateString("kty",kty);
//        json.UpdateString("n",n);
//        json.UpdateString("e",e);
//
//        //  Note: The JSON can contain other members, such as "use", "kid", or anything else.  These will be ignored.
//        json.put_EmitCompact(false);
//
//        //  Show the JWK string to be loaded:
//        String jwkStr = json.emit();
//
//        CkPublicKey pubKey = new CkPublicKey();
//        //  The LoadFromString method will automatically detect the format.
//        success = pubKey.LoadFromString(jwkStr);
//        if (success != true) {
//            System.out.println(pubKey.lastErrorText());
//            return "";
//        }
//
//        //  OK.. the JWK is loaded.  It can be used in whatever way desired...
//
//        //  The key can be retrieved in any other format, such as XML or PEM..
//        System.out.println(pubKey.getXml());
//
//        //  XML output:
//        //  <RSAPublicKey>
//        //      <Modulus>33TqqLR3eeUmDtHS89qF3p4MP7Wfqt2Zjj3lZjLjjCGDvwr9cJNlNDiuKboODgUiT4ZdPWbOiMAfDcDzlOxA04DDnEFGAf+kDQiNSe2ZtqC7bnIc8+KSG/qOGQIVaay4Ucr6ovDkykO5Hxn7OU7sJp9TP9H0JH8zMQA6YzijYH9LsupTerrY3U6zyihVEDXXOv08vBHk50BMFJbE9iwFwnxCsU5+UZUZYw87Uu0n4LPFS9BT8tUIvAfnRXIEWCha3KbFWmdZQZlyrFw0buUEf0YN3/Q0auBkdbDR/ES2PbgKTJdkjc/rEeM0TxvOUf7HuUNOhrtAVEN1D5uuxE1WSw==</Modulus>
//        //      <Exponent>AQAB</Exponent>
//        //  </RSAPublicKey>
//
//        //  Choose PCKS1 or PCKS8 PEM format..
//        boolean bPreferPkcs1 = false;
//        System.out.println(pubKey.getPem(bPreferPkcs1));
//        return pubKey.getPem(bPreferPkcs1);
//        //  PEM output
//        //  -----BEGIN PUBLIC KEY-----
//        //  MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA33TqqLR3eeUmDtHS89qF
//        //  3p4MP7Wfqt2Zjj3lZjLjjCGDvwr9cJNlNDiuKboODgUiT4ZdPWbOiMAfDcDzlOxA
//        //  04DDnEFGAf+kDQiNSe2ZtqC7bnIc8+KSG/qOGQIVaay4Ucr6ovDkykO5Hxn7OU7s
//        //  Jp9TP9H0JH8zMQA6YzijYH9LsupTerrY3U6zyihVEDXXOv08vBHk50BMFJbE9iwF
//        //  wnxCsU5+UZUZYw87Uu0n4LPFS9BT8tUIvAfnRXIEWCha3KbFWmdZQZlyrFw0buUE
//        //  f0YN3/Q0auBkdbDR/ES2PbgKTJdkjc/rEeM0TxvOUf7HuUNOhrtAVEN1D5uuxE1W
//        //  SwIDAQAB
//        //  -----END PUBLIC KEY-----
//    }
//}
