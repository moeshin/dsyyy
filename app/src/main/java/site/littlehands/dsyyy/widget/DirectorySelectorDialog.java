package site.littlehands.dsyyy.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import site.littlehands.dsyyy.R;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class DirectorySelectorDialog extends AlertDialog
        implements AdapterView.OnItemClickListener, DialogInterface.OnClickListener {

    private static class Adapter extends ArrayAdapter<File> {

        public Adapter(Context context, List<File> dirs) {
            super(context, 0, dirs);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder") View v = View.inflate(getContext(), R.layout.directory_list_item, null);

            TextView name = v.findViewById(R.id.directory_name);

            ImageView icon = v.findViewById(R.id.directory_icon);

            File f = getItem(position);
            if (f == null) {
                name.setText(R.string.directory_selector_dialog_parent);
                icon.setImageResource(R.drawable.ic_action_up);
            } else {
                name.setText(f.getName());
                icon.setImageResource(R.drawable.ic_action_folder);
            }
            return (v);
        }

    }

    private class Comparator implements java.util.Comparator<File> {

        private int sort;

        private int order;

        private Comparator(int sort, int order) {
            this.sort = sort;
            this.order = order;
        }

        @SuppressWarnings("DuplicateExpressions")
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs == null || rhs == null) {
                return (1);
            }
            switch (sort) {
                case SORT_MODIFIED:
                    return (order * Long.compare(lhs.lastModified(), rhs.lastModified()));
                default:
                    return order * lhs.getName().compareTo(rhs.getName());
            }

        }

    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class Helper {

        private Context context;

        private File directory = Environment.getExternalStorageDirectory();

        private int sortBy = DirectorySelectorDialog.SORT_NAME;

        private int orderBy = DirectorySelectorDialog.ORDER_ASCENDING;

        public Helper(Context context) {
            this.context = context;
        }

        public Helper directory(File directory) {
            this.directory = directory;
            return this;
        }

        public Helper sortBy(int sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Helper orderBy(int orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public DirectorySelectorDialog build()  {
            return new DirectorySelectorDialog(context, directory, sortBy, orderBy);
        }

    }

    public interface onSelectListener {
        /**
         * Event triggered when a directory is selected.
         *
         * @param directory	The selected directory
         */
        void onDirectorySelected(File directory);
    }

    public static final int SORT_NAME = 0;

    public static final int SORT_MODIFIED = 1;

    public static final int ORDER_ASCENDING = 1;

    public static final int ORDER_DESCENDING = -1;

    private File current;

    private onSelectListener onSelectListener;

    private ArrayAdapter<File> listAdapter;

    private Comparator comparator;

    private TextView pathView;

    private DirectorySelectorDialog(@NonNull Context context, File directory, int sortBy, int orderBy)
            throws IllegalArgumentException {

        super(context);

        if (directory == null) {
            throw new IllegalArgumentException("entry directory is null");
        }

        if (!directory.exists()) {
            throw new IllegalArgumentException("entry directory does not exist");
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " is not a directory");
        }

        if (!directory.canRead()) {
            throw new IllegalArgumentException(directory.getAbsolutePath() + " is not readable");
        }

        if (sortBy != SORT_NAME && sortBy != SORT_MODIFIED) {
            throw new IllegalArgumentException("invalid sort criteria");
        }

        if (orderBy != ORDER_ASCENDING && orderBy != ORDER_DESCENDING) {
            throw new IllegalArgumentException("invalid order criteria");
        }

        setTitle(R.string.directory_selector_dialog_title);
        setCancelable(false);

        View layout = LayoutInflater.from(context).inflate(R.layout.directory_selector_dialog, null);

        setView(layout);

        listAdapter = new Adapter(getContext(), new ArrayList<File>());
        listAdapter.setNotifyOnChange(true);

        pathView =  layout.findViewById(R.id.directory_selector_current);

        ListView listView = layout.findViewById(R.id.directory_selector_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel), (OnClickListener) null);
        setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.directory_selector_dialog_button_select), this);


        current = directory;

        comparator = new Comparator(sortBy, orderBy);

        list(current);
    }

    public DirectorySelectorDialog setOnSelectListener(onSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
        return this;
    }

    private void list(File directory) {
        pathView.setText(current.getAbsolutePath());
        listAdapter.clear();
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isDirectory());
            }
        });
        if (directory.getParent() != null) {
            listAdapter.add(null);
        }
        if (files != null) {
            for (File file : files) {
                listAdapter.add(file);
            }
        }
        listAdapter.sort(comparator);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (onSelectListener != null) {
            onSelectListener.onDirectorySelected(current);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        File file = listAdapter.getItem(arg2);
        if (file == null) {
            current = current.getParentFile();
        } else {
            current = file;
        }
        if (current != null) {
            list(current);
        }
    }

    public static DirectorySelectorDialog show(Context context, onSelectListener listener) {
        DirectorySelectorDialog dialog = new Helper(context).build();
        dialog.setOnSelectListener(listener).show();
        return dialog;
    }

    public static DirectorySelectorDialog show(Context context, File dir, onSelectListener listener) {
        DirectorySelectorDialog dialog = new Helper(context).directory(dir).build();
        dialog.setOnSelectListener(listener).show();
        return dialog;
    }
}
