package bt.assetmanager.views;

import bt.assetmanager.views.images.ImagesView;
import bt.assetmanager.views.import_.ImportView;
import bt.assetmanager.views.options.OptionsView;
import bt.assetmanager.views.sounds.SoundsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.RouterLink;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout
{

    public MainLayout()
    {
        addToNavbar(createHeaderContent());
    }

    private Component createHeaderContent()
    {
        Header header = new Header();
        header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "flex-col", "w-full");
        Nav nav = new Nav();
        nav.addClassNames("flex", "gap-s", "overflow-auto", "px-m");

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames("flex", "list-none", "m-0", "p-0");
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems())
        {
            list.add(menuItem);
        }

        header.add(nav);
        return header;
    }

    private MenuItemInfo[] createMenuItems()
    {
        return new MenuItemInfo[] {
                new MenuItemInfo("Images", "la la-image", ImagesView.class),
                new MenuItemInfo("Sounds", "la la-music", SoundsView.class),
                new MenuItemInfo("Import", "la la-upload", ImportView.class),
                new MenuItemInfo("Options", "la la-cog", OptionsView.class)
        };
    }

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem
    {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, String iconClass, Class<? extends Component> view)
        {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames("flex", "h-m", "items-center", "px-s", "relative", "text-secondary");
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames("font-medium", "text-s", "whitespace-nowrap");

            link.add(new LineAwesomeIcon(iconClass), text);
            add(link);
        }

        public Class<?> getView()
        {
            return view;
        }

        /**
         * Simple wrapper to create icons using LineAwesome iconset. See
         * https://icons8.com/line-awesome
         */
        @NpmPackage(value = "line-awesome", version = "1.3.0")
        public static class LineAwesomeIcon extends Span
        {
            public LineAwesomeIcon(String lineawesomeClassnames)
            {
                // Use Lumo classnames for suitable font size and margin
                addClassNames("me-s", "text-l");
                if (!lineawesomeClassnames.isEmpty())
                {
                    addClassNames(lineawesomeClassnames);
                }
            }
        }

    }

}
