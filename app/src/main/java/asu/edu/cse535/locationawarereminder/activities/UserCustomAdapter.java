package asu.edu.cse535.locationawarereminder.activities;

/**
 * Created by CHE on 11/13/2016.
 */



/**
 * Created by CHE on 11/12/2016.
 */

        import android.app.Activity;
        import android.content.Context;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.util.ArrayList;
        import asu.edu.cse535.locationawarereminder.R;
        import asu.edu.cse535.locationawarereminder.database.DBManager;


        import android.app.Activity;
        import android.content.Context;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.util.ArrayList;



/**
 * Created by CHE on 11/12/2016.
 */

        import java.util.ArrayList;
        import android.app.Activity;
        import android.content.Context;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.TextView;
        import android.widget.Toast;

public class UserCustomAdapter extends ArrayAdapter<String>  {
    String t_name;
    Context context;
    int layoutResourceId;
    ArrayList<String> data = new ArrayList<String>();

    public UserCustomAdapter(Context context, int layoutResourceId,
                             ArrayList<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        UserHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new UserHolder();
            holder.textName = (TextView) row.findViewById(R.id.rowTextView);
            //holder.textAddress = (TextView) row.findViewById(R.id.textView2);
            //holder.textLocation = (TextView) row.findViewById(R.id.textView3);
            holder.Mark_done = (Button) row.findViewById(R.id.button1);
            holder.btnDelete = (Button) row.findViewById(R.id.button2);
            row.setTag(holder);
        } else {
            holder = (UserHolder) row.getTag();
        }
        String user = data.get(position);
        holder.textName.setText(user);
        t_name = holder.textName.getText().toString();
        //holder.textAddress.setText(user.getAddress());
        //holder.textLocation.setText(user.getLocation());
        holder.Mark_done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub,
                Toast.makeText(context, "Mark_done button Clicked",
                        Toast.LENGTH_LONG).show();
                DBManager.Update_task(t_name);
                //MainActivity main = new MainActivity();
                //main.load_tasks_from_db();


            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.i("Delete Button Clicked", "**********");
                Toast.makeText(context, "Delete button Clicked",
                        Toast.LENGTH_LONG).show();
                Log.v("t_name_Del",t_name);
                DBManager.DeleteTask(t_name);
                //MainActivity main = new MainActivity();
                // main.load_tasks_from_db();

            }
        });
        return row;

    }

    static class UserHolder {
        TextView textName;
        Button Mark_done;
        Button btnDelete;
    }
}





