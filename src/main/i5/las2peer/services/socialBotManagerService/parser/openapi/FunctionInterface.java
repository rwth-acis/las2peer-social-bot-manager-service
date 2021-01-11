package i5.las2peer.services.socialBotManagerService.parser.openapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;

public interface FunctionInterface {
	
	public String getFunctionName();

	public String getBasePath();

	public String getFunctionPath();

	public String getHttpMethod();

	public Collection<ServiceFunctionAttribute> getAllAttributes();

	public default boolean contains(ServiceFunctionAttribute attr) {
		assert attr != null;
		
		for (ServiceFunctionAttribute attr2 : this.getAllAttributes())
			if (attr.equals(attr2))
				return true;
		
		return false;
	}
	
	public default ServiceFunctionAttribute getAttribute(String name) {
		assert name != null;
		
		Collection<ServiceFunctionAttribute> attributes = this.getAllAttributes();
		for (ServiceFunctionAttribute attr : attributes)
			if (attr.getIdName().equalsIgnoreCase(name) || attr.getId().contentEquals(name))
				return attr;
		
		return null;
	}
	
	public default List<ServiceFunctionAttribute> getRequiredAttributes() {
		assert this.getAllAttributes() != null;
		
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.getAllAttributes()) 
			if (attr.isRequired())
				res.add(attr);
		
		return res;
	}
	
	public default List<ServiceFunctionAttribute> getPathAttributes() {
		assert this.getAllAttributes() != null;
		
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.getAllAttributes()) {
			if (attr.getParameterType() == ParameterType.PATH)
				res.add(attr);
		}
		return res;
	}

	public default List<ServiceFunctionAttribute> getQueryAttributes() {
		assert this.getAllAttributes() != null;
		
		List<ServiceFunctionAttribute> res = new ArrayList<ServiceFunctionAttribute>();
		for (ServiceFunctionAttribute attr : this.getAllAttributes()) {
			if (attr.getParameterType() == ParameterType.QUERY)
				res.add(attr);
		}
		return res;
	}
	
	public ServiceFunction asServiceFunction();

}
