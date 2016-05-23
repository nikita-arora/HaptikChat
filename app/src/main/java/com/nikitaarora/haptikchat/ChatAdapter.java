package com.nikitaarora.haptikchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    Context mContext;
    Picasso mPicasso;
    JSONArray jsonArray;

    public ChatAdapter (JSONArray jsonArray, Context context)   {
        super();
        this.jsonArray = sortJsonArray(jsonArray);
        this.mContext = context;
        this.mPicasso = Picasso.with(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtUser, txtMessageBody, txtMessageInfo, txtNameYou, txtMessageBodyYou, txtMessageInfoYou;
        ImageView imgUser, imgYou;
        LinearLayout userLayout, yourLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            imgUser = (ImageView) itemView.findViewById(R.id.imgUser);
            imgYou = (ImageView) itemView.findViewById(R.id.imgUser_you);

            txtUser = (TextView) itemView.findViewById(R.id.lblMsgFrom);
            txtNameYou = (TextView) itemView.findViewById(R.id.lblMsgFrom_you);
            txtMessageBody = (TextView) itemView.findViewById(R.id.txtMsg);
            txtMessageBodyYou = (TextView) itemView.findViewById(R.id.txtMsg_you);
            txtMessageInfo = (TextView) itemView.findViewById(R.id.msgInfo);
            txtMessageInfoYou = (TextView) itemView.findViewById(R.id.msgInfo_you);

            userLayout = (LinearLayout) itemView.findViewById(R.id.left_layout);
            yourLayout = (LinearLayout) itemView.findViewById(R.id.right_layout);
        }
    }
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
        final ChatMessages messages = new ChatMessages();
            JSONObject jsonObject;
            try {
                jsonObject = jsonArray.getJSONObject(position);
                messages.setImageUrl(jsonObject.getString(Config.TAG_IMAGE_URL));
                messages.setName(jsonObject.getString(Config.TAG_USER_NAME));
                messages.setUserName(jsonObject.getString(Config.TAG_USERNAME));
                messages.setMessageBody(jsonObject.getString(Config.TAG_MESSAGE_BODY));
                messages.setMessageInfo(jsonObject.getString(Config.TAG_MESSAGE_INFO));
            } catch (Exception e)   {
                e.printStackTrace();
            }

        if (messages.getUserName().equalsIgnoreCase("ryan-a"))   {
            holder.userLayout.setVisibility(View.GONE);
            holder.yourLayout.setVisibility(View.VISIBLE);

            if (messages.getImageUrl().isEmpty()) {
                mPicasso.load(R.mipmap.haptik).into(holder.imgYou);
            } else {
                mPicasso.load(messages.getImageUrl()).into(holder.imgYou);
            }

            holder.txtNameYou.setText(messages.getName());
            holder.txtMessageBodyYou.setText(messages.getMessageBody());
            holder.txtMessageInfoYou.setText(String.valueOf(messages.getMessageInfo()));
        }

        else    {
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.yourLayout.setVisibility(View.GONE);
            if (messages.getImageUrl().isEmpty()) {
                mPicasso.load(R.mipmap.haptik).into(holder.imgUser);
            } else {
                mPicasso.load(messages.getImageUrl()).into(holder.imgUser);
            }
            holder.txtUser.setText(messages.getName());
            holder.txtMessageBody.setText(messages.getMessageBody());
            holder.txtMessageInfo.setText(String.valueOf(messages.getMessageInfo()));
        }

        holder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCountDialog(messages);
            }
        });
        holder.yourLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCountDialog(messages);
            }
        });
    }

    private void getCountDialog(ChatMessages msg)   {
        String username = msg.getUserName();
        int messageCount = 0;
        for (int i=0; i<jsonArray.length(); i++)    {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                if ((object.getString(Config.TAG_USERNAME)).equals(username))  {
                    messageCount++;
                }
            }   catch (Exception e) {
                e.printStackTrace();
            }
        }
        buildDialog(mContext, msg.getName(), messageCount);
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public void buildDialog(Context context, String user_name, int count)    {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Messages from "+user_name+": " +count)
                    .setCancelable(true)
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener()  {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }   catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long convertToMilliSeconds(String date) {
        long timeInMilli = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date mDate = sdf.parse(date);
            timeInMilli = mDate.getTime();
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return timeInMilli;
    }

    private String convertToDateTime(long milliseconds)  {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    private JSONArray sortJsonArray(JSONArray unsorted) {
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonList = new ArrayList<>();
        try{
            for (int i = 0; i < unsorted.length(); i++) {
                unsorted.getJSONObject(i).put("message-time",
                        convertToMilliSeconds(unsorted.getJSONObject(i).getString("message-time")));
                jsonList.add(unsorted.getJSONObject(i));
            }
            Collections.sort(jsonList, new Comparator<JSONObject>() {

                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();
                    try {
                        valA = String.valueOf(a.get("message-time"));
                        valB = String.valueOf(b.get("message-time"));
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return valA.compareTo(valB);
                }
            });
            for (int i = 0; i < unsorted.length(); i++) {
                sortedJsonArray.put(jsonList.get(i));
                sortedJsonArray.getJSONObject(i).put("message-time",
                        convertToDateTime(sortedJsonArray.getJSONObject(i).getLong("message-time")));
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return sortedJsonArray;
    }
}
