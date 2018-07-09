import com.morningtech.eth.server.util.StringUtils;
import org.web3j.utils.Numeric;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: Test
 * @Package PACKAGE_NAME
 * @Description: TODO
 * @date 2018/6/24 13:50
 */
public class Test {

    public  String test(String value) throws Exception {
        if(!StringUtils.isNullOrEmpty(value) && Numeric.containsHexPrefix(value)) {
            String code = Numeric.toBigInt(value).toString();
            return code;
        }
        throw new Exception();
    }

    @org.junit.Test
    public void test(){
        try {
            System.out.println(test("0x53ba02"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
