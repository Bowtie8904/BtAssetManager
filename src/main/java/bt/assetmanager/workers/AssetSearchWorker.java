package bt.assetmanager.workers;

import bt.assetmanager.components.AssetSearchPanel;
import com.vaadin.flow.component.UI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
public class AssetSearchWorker<T> implements BackgroundWorker
{
    private AssetSearchPanel searchPanel;
    private Runnable onFinish;

    public AssetSearchWorker(AssetSearchPanel searchPanel)
    {
        this.searchPanel = searchPanel;
    }

    @Override
    public void work(UI ui)
    {
        ui.access(() -> {
            this.searchPanel.getProgressBar().setIndeterminate(true);
            this.searchPanel.getProgressBar().setVisible(true);
            this.searchPanel.getProgressLabel().setText("Searching...");
        });

        List<T> resultSet = List.of();

        if (this.searchPanel.getSearchTextField().getValue().trim().isEmpty())
        {
            resultSet = this.searchPanel.getAssetService().findAll();
        }
        else
        {
            if (this.searchPanel.getFileNameFilterCheckbox().getValue())
            {
                resultSet = this.searchPanel.getAssetService().findByFileName(this.searchPanel.getSearchTextField().getValue().trim());
            }
            else
            {
                String[] singleTags = this.searchPanel.getSearchTextField().getValue().split(",");
                List<String> singleTagList = Arrays.asList(singleTags).stream().map(String::trim).map(String::toUpperCase).collect(Collectors.toList());

                resultSet = this.searchPanel.getAssetService().findByTags(singleTagList);
            }
        }

        List<T> finalResultSet = resultSet;

        ui.access(() -> this.searchPanel.getProgressLabel().setText("Filling view..."));

        ui.access(() -> {
            this.searchPanel.getFoundFilesLabel().setText(finalResultSet.size() + " files found");

            if (this.searchPanel.getOnSearchConsumer() != null)
            {
                this.searchPanel.getOnSearchConsumer().accept(finalResultSet);
            }

            if (this.onFinish != null)
            {
                this.onFinish.run();
            }
        });

        ui.setPollInterval(-1);
    }

    public void onFinish(Runnable onFinish)
    {
        this.onFinish = onFinish;
    }
}