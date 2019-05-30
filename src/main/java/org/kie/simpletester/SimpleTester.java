package org.kie.simpletester;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.sample.Applicant;
import org.drools.core.command.impl.CommandFactoryServiceImpl;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

public class SimpleTester {
	

    private static final String CONTAINER_ID = "SampleScore_1.0.01";

    private static URL url;

    static {
        try {
            url = new URL("http://localhost:8080/kie-server/services/rest/server");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private KieServicesClient getClient(long timeout) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(url.toExternalForm(), "kieserver", "kieserver1!");
        configuration.setMarshallingFormat(MarshallingFormat.JAXB);
        Set<Class<?>> extraClasses = new HashSet<>();
        extraClasses.add(Applicant.class);
        configuration.addExtraClasses(extraClasses);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        return kieServicesClient;

    }

    public SimpleTester() {
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();
        KieServicesClient client = (KieServicesClient) getClient(10000);
        RuleServicesClient rsclient = client.getServicesClient(RuleServicesClient.class);

        ServiceResponse<KieContainerResource> reply = client.getContainerInfo(CONTAINER_ID);
        if (reply != null) {
            System.out.println("Got container resource");
        } else {
            System.out.println("Didn't get container resource");
        }

        PMMLRequestData request = createSimpleScorecardRequest("123", "Sample Score", 33.0, "SKYDIVER", "KN", true);
        ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory).newApplyPmmlModel(request);
        command.setPackageName("com.sample");

        ServiceResponse<ExecutionResults> execResults = rsclient.executeCommandsWithResults(CONTAINER_ID, command);
        if (execResults != null) {
            System.out.println("Got execution results!");
            if (execResults.getType().equals(ResponseType.SUCCESS)) {
                System.out.println("And it was successful!");
                PMML4Result holder = (PMML4Result) execResults.getResult().getValue("results");
                if (holder != null) {
                    System.out.println("!!! RESULTS !!!");
                    System.out.println(holder);
                } else {
                    System.out.println("No PMML results");
                }
            } else {
                System.out.println("But there was an error");
                System.out.println(execResults.getMsg());
            }
        } else {
            System.out.println("But we didn't get any results from our command");
        }
    }

	
	public static void main(String[] args) {
        new SimpleTester();
		System.exit(0);
	}
	
    private static PMMLRequestData createSimpleScorecardRequest(String correlationId, 
            String model, 
            Double age, 
            String occupation, 
            String residenceState, 
            boolean validLicense) {
        PMMLRequestData data = new PMMLRequestData(correlationId,model);
        data.addRequestParam("age", age);
        data.addRequestParam("occupation", occupation);
        data.addRequestParam("residenceState", residenceState);
        data.addRequestParam("validLicense", validLicense);
        return data;
    }

}
