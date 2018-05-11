package electon;

import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/10
 */
@Data
public class ServiceNode {

    private String url;

    private String name;

    private Boolean isMaster;

    public ServiceNode(String url, String name, Boolean isMaster) {
        this.url = url;
        this.name = name;
        this.isMaster = isMaster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if(url == null){
            return false;
        }

        ServiceNode that = (ServiceNode) o;

        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        return result;
    }
}
