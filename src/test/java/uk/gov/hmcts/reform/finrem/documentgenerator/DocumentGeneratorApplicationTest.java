package uk.gov.hmcts.reform.finrem.documentgenerator;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.documentgenerator.category.SmokeTest;

@RunWith(SpringRunner.class)
@Category(SmokeTest.class)
@SpringBootTest
public class DocumentGeneratorApplicationTest {

    @Autowired
    private ApplicationContext applicationArguments;

    @Test
    public void contextLoads() {
        applicationArguments.getApplicationName();
        applicationArguments.getStartupDate();
    }
}
