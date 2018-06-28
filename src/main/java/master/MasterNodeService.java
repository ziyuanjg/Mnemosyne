package master;

import common.BizResult;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 * Created by Mr.Luo on 2018/5/23
 */
@Path("master")
public class MasterNodeService {


    @Path("heart")
    @GET
    public BizResult heart(@Context HttpServletRequest request) {
        return BizResult.createSuccessResult(null);
    }



}
