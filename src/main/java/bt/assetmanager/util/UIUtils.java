package bt.assetmanager.util;

import com.vaadin.flow.component.html.Span;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public final class UIUtils
{
    private UIUtils()
    {

    }

    public static Span heightFiller(String height)
    {
        Span span = new Span();
        span.setHeight(height);
        return span;
    }

    public static Span widthFiller(String width)
    {
        Span span = new Span();
        span.setWidth(width);
        return span;
    }
}