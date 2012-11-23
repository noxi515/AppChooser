package jp.co.noxi.apppicker.widget;

import jp.co.noxi.apppicker.R;
import jp.co.noxi.apppicker.content.Component;
import jp.co.noxi.apppicker.content.ComponentHolder;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ComponentAdapterFroyo extends ComponentAdapter {

    private static class ViewHolder implements ViewBinder {
        ImageView icon;
        TextView label;
    }

    public ComponentAdapterFroyo(Activity activity, int layoutResId,
            ComponentHolder components) {
        super(activity, layoutResId, components);
    }

    @Override
    protected ViewBinder newView(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.icon = (ImageView) convertView.findViewById(R.id.icon);
        holder.label = (TextView) convertView.findViewById(R.id.label);
        return holder;
    }

    @Override
    protected void bindView(Component component, ViewBinder binder, int position) {
        ViewHolder holder = (ViewHolder) binder;
        holder.icon.setImageDrawable(component.icon);
        holder.label.setText(component.label);
    }

}
