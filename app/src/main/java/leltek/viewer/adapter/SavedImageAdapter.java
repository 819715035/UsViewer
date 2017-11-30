package leltek.viewer.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import leltek.viewer.R;

/**
 * Created by rajesh on 4/10/17.
 */

public class SavedImageAdapter extends RecyclerView.Adapter<SavedImageAdapter.SavedImageViewHolder> {
    private String[] data;
    SavedImageViewHolder holder;

    public SavedImageAdapter(String[] data, ICallBack iCallBack) {
        this.data = data;
        this.iCallBack = iCallBack;
    }

    @Override
    public SavedImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SavedImageViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.row_saved_image, parent, false));
    }

    @Override
    public void onBindViewHolder(final SavedImageViewHolder holder, int position) {
        if (position == 0) {
            this.holder = holder;
            holder._itemView.setBackgroundColor(Color.YELLOW);
        }

        if (data[position] != null && !data[position].isEmpty()) {
            Picasso.with(holder.itemView.getContext()).load(new File(data[position])).into(holder.capturedImage);
        }
        holder.capturedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SavedImageAdapter.this.holder._itemView.setBackgroundColor(Color.GRAY);
                holder._itemView.setBackgroundColor(Color.YELLOW);
                SavedImageAdapter.this.holder = holder;
                iCallBack.showImage(data[holder.getAdapterPosition()]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.length;
    }

    class SavedImageViewHolder extends RecyclerView.ViewHolder {
        ImageView capturedImage;
        View _itemView;

        SavedImageViewHolder(View itemView) {
            super(itemView);
            this._itemView = itemView;
            capturedImage = itemView.findViewById(R.id.capturedImage);
        }
    }

    private ICallBack iCallBack;

    public interface ICallBack {
        void showImage(String image);
    }
}
