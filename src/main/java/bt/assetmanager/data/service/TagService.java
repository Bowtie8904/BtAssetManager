package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
@Service
public class TagService implements Serializable
{
    @Autowired
    private TagRepository tagRepo;

    public Tag obtainTag(String name)
    {
        if (this.tagRepo.existsByNameIgnoreCase(name))
        {
            return this.tagRepo.findByNameIgnoreCase(name);
        }

        Tag newTag = new Tag();
        newTag.setName(name.toUpperCase());

        newTag = this.tagRepo.save(newTag);

        return newTag;
    }

    public List<String> getTagNamesForValue(String partialTagName)
    {
        return this.tagRepo.findFirst5ByNameContainingIgnoreCase(partialTagName)
                           .stream()
                           .map(Tag::getName)
                           .sorted()
                           .toList();
    }
}