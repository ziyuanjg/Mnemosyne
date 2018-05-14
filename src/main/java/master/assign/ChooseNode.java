package master.assign;

import electon.ElectonConfig;
import electon.ServiceNode;
import java.util.List;
import java.util.Random;

/**
 * Created by Mr.Luo on 2018/5/14
 */
public interface ChooseNode {

    ServiceNode choose();

    class DefaultChooseNode implements ChooseNode{

        private List<ServiceNode> serviceNodeList = ElectonConfig.getServiceNodeList();

        @Override
        public ServiceNode choose() {
            return serviceNodeList.get(new Random().nextInt(serviceNodeList.size()));
        }
    }
}



