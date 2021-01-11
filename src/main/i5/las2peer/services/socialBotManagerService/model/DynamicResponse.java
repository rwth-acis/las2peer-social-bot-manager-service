package i5.las2peer.services.socialBotManagerService.model;

public interface DynamicResponse {

	public GeneratorFunction getGeneratorFunction();
	
	public  String getActIntent();
	
	public default boolean hasDynamicResponse() {
		return getGeneratorFunction() != null;
	}
	
}
