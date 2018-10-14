package test.spring;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import oris.Application;
import oris.Profiles;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(Profiles.TEST)
@ContextConfiguration(classes = {Application.class, SpringTestConfiguration.class})
public class BaseSpringTest {


}