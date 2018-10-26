package uk.gov.hmcts.reform.finrem;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class GeneratePDFIntegrationTest {

    @Test
    public void simple() throws Exception {
        Assert.assertEquals(HttpStatus.OK.value(), 200);
    }

}
