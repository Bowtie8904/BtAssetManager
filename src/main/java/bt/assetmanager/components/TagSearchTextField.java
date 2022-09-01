package bt.assetmanager.components;

import bt.assetmanager.data.service.TagService;
import com.vaadin.componentfactory.Autocomplete;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public class TagSearchTextField extends Autocomplete
{
    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private TagService tagService;
    private String currentTagTextFieldValue;
    private boolean processAutoCompleteApplyEvent = true;

    public TagSearchTextField(int limit, TagService tagService)
    {
        super(limit);

        this.tagService = tagService;

        addChangeListener(event -> {
            String text = event.getValue();
            String[] singleTags = text.split(",");

            String currentTag = singleTags[singleTags.length - 1].trim();

            if (!currentTag.isEmpty())
            {
                setOptions(this.tagService.getTagNamesForValue(currentTag));
            }
            else
            {
                setOptions(List.of());
            }

            this.currentTagTextFieldValue = event.getValue();
        });

        addAutocompleteValueAppliedListener(e -> {
            if (this.processAutoCompleteApplyEvent)
            {
                this.processAutoCompleteApplyEvent = false;

                String[] singleTags = this.currentTagTextFieldValue.split(",");
                List<String> newTagList = Arrays.asList(singleTags).stream().map(String::trim).collect(Collectors.toList());
                newTagList.set(newTagList.size() - 1, e.getValue());

                setValue(String.join(", ", newTagList));

                executorService.schedule(() -> this.processAutoCompleteApplyEvent = true, 200, TimeUnit.MILLISECONDS);
            }
        });
    }
}