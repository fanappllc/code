package com.special.ResideMenu;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * User: special
 * Date: 13-12-10
 * Time: 下午11:05
 * Mail: specialcyci@gmail.com
 */
public class ResideMenuItem extends LinearLayout {

    /**
     * menu item  icon
     */
    private ImageView iv_icon;
    /**
     * menu item  title
     */
    private TextView tv_title;
    private TextView textView_noti_count;

    private LinearLayout relativeLayout;

    int getScreenWidth;

    public ResideMenuItem(Context context) {
        super(context);
        initViews(context);
    }

    public ResideMenuItem(Context context, int icon, int title) {
        super(context);
        initViews(context);
        iv_icon.setImageResource(icon);
        tv_title.setText(title);
    }

    public ResideMenuItem(Context context, int icon, String title, int width) {
        super(context);
        setwidth(width);
        initViews(context);
        iv_icon.setImageResource(icon);
        tv_title.setText(Html.fromHtml(title));

    }

    /*public ResideMenuItem(Context context, String title, int pos) {
        super(context);
        if (pos == 5) {
            textView_noti_count.setText(title);
        } else {
            textView_noti_count.setVisibility(GONE);
        }

    }*/


    public void updateCount(String count) {
        if (count.equalsIgnoreCase("0")) {
            textView_noti_count.setVisibility(GONE);
        } else {
            textView_noti_count.setVisibility(VISIBLE);
            textView_noti_count.setText(count);
        }

    }

//    @SuppressWarnings("deprecation")
//    public  Spanned fromHtml(String html) {
//        Spanned result;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
//        } else {
//            result = Html.fromHtml(html);
//        }
//        return result;
//    }


    public void setwidth(int width) {
        getScreenWidth = width;
    }

    private void initViews(Context context) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.residemenu_item, this);
        textView_noti_count = (TextView) findViewById(R.id.textView_noti_count);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_title = (TextView) findViewById(R.id.tv_title);
        relativeLayout = (LinearLayout) findViewById(R.id.relativeLayout);
        LinearLayout.LayoutParams relativeLayoutparam = new LinearLayout.LayoutParams((getScreenWidth * 58) / 100, LinearLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(relativeLayoutparam);

    }

    /**
     * set the icon color;
     *
     * @param icon
     */
    public void setIcon(int icon) {
        iv_icon.setImageResource(icon);
    }

    /**
     * set the title with resource
     * ;
     *
     * @param title
     */
    public void setTitle(int title) {
        tv_title.setText(title);
    }

    public void setResideMenuItemSelect(boolean isSelect) {

        if (isSelect == true) {
            // int mycolor = getResources().getColor(R.color.white);
            //iv_icon.getDrawable().setColorFilter(mycolor, PorterDuff.Mode.SRC_ATOP);

            relativeLayout.setBackgroundColor(Color.parseColor("#955AC8F9"));
            //tv_title.setTextColor(getResources().getColor(R.color.white));
        } else {
            //int mycolor = getResources().getColor(R.color.voilet);
            // iv_icon.getDrawable().setColorFilter(mycolor, PorterDuff.Mode.SRC_ATOP);
            relativeLayout.setBackgroundColor(Color.parseColor("#00000000"));
            //tv_title.setTextColor(getResources().getColor(R.color.voilet));
        }
    }


    /**
     * set the title with string;
     *
     * @param title
     */
    public void setTitle(String title) {
        tv_title.setText(title);
    }
}
