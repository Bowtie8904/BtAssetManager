package bt.assetmanager.data.generator;

import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.service.TagRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class DataGenerator
{

    @Bean
    public CommandLineRunner loadData(TagRepository repo)
    {
        return args -> {
            for (int i = 0; i < 200; i++)
            {
                Tag tag = new Tag();
                tag.setName("exampleTag" + i);

                repo.save(tag);
            }
        };
    }

}