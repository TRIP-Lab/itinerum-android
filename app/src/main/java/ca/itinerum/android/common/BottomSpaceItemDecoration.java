package ca.itinerum.android.common;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BottomSpaceItemDecoration extends RecyclerView.ItemDecoration {
	private int mSpace;

	public BottomSpaceItemDecoration(int space) {
		mSpace = space;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1){
			outRect.bottom = mSpace;
		}
	}
}