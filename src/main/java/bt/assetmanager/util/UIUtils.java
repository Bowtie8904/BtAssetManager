package bt.assetmanager.util;

import com.vaadin.flow.component.html.Span;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public final class UIUtils
{
    public static Span span(String height)
    {
        Span span = new Span();
        span.setHeight(height);
        return span;
    }
}