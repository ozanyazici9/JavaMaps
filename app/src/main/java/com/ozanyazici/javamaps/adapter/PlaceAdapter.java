package com.ozanyazici.javamaps.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Placeholder;
import androidx.recyclerview.widget.RecyclerView;

import com.ozanyazici.javamaps.MapsActivity;
import com.ozanyazici.javamaps.databinding.RecyclerRowBinding;
import com.ozanyazici.javamaps.model.Place;

import java.util.List;


public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    List<Place> placeList;

    public PlaceAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override //Görünüm tutucu oluşturulduğunda ne olacak; recycler_row.xml i RecyclerRowBinding aracılığıyla kodu bağlayacağız
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(recyclerRowBinding);
        //Bu bağlantıyı kullanmak için bağlantının atandığı değişeni parametre vererek PlaceHolder nesnesi oluşturuyorum.

        /*
        recyclerRowBinding, bir değişken olup XML görünümünün temsilini taşır ve bu görünüme erişmek ve içeriğini ayarlamak için kullanılır.
        PlaceHolder sınıfının bir örneği oluşturulurken bu değişken parametre olarak kullanılır,
        böylece PlaceHolder nesnesi, bu görünümü temsil eden bir nesne haline gelir.
         */

        /*
        Her satır için yalnızca bir tane `PlaceHolder` nesnesi oluşturulur
        ve bu nesne, RecyclerView içindeki farklı satırların görünümlerini temsil eder.
        Yani, her bir satır için ayrı bir `PlaceHolder` nesnesi oluşturulmaz. Bu nesne, sırayla her satırın görünümünü bağlamak için kullanılır.
        Bu, bellek kullanımını optimize eder ve veri bağlamayı daha verimli hale getirir.
        */
        /*
        Bu kod parçasında `RecyclerRowBinding` nesnesi oluşturuluyor ve bu nesne, `RecyclerView`'un her bir satırının XML görünümünü temsil eder.
        Bu görünüm, `RecyclerRowBinding.inflate(...)` ile şişirilir ve ardından bu nesne, `PlaceHolder` sınıfının bir örneği oluşturulurken parametre olarak verilir.
        Yani, `PlaceHolder` sınıfı, her bir satırın görünümünü ve içeriğini temsil etmek için bu `RecyclerRowBinding` nesnesini kullanır.
        Bu sayede `onBindViewHolder` içinde `PlaceHolder` aracılığıyla bu görünümlere erişebilir ve içerikleri ayarlayabilirsiniz
        Bu, verileri `RecyclerView` içindeki satırlara bağlamak için kullanılan bir yaygın yöntemdir.
         */

    }

    @Override //Görünüm Tutucu kod ile bağlandığında ne olacak
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {

        holder.recyclerRowBinding.recyclerViewTextView.setText(placeList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(holder.getAdapterPosition()));
                holder.itemView.getContext().startActivity(intent);
            }
        });
        /*
       `onBindViewHolder` metodunun parametresi olarak gelen `holder` değişkeni, `PlaceHolder` sınıfının bir örneğini temsil eder.
        Bu `holder` nesnesi, her bir satırın görünümünü ve içeriğini temsil eder.
        Bu nedenle `holder` üzerinden satırın görünümlerine erişebilirsiniz ve bu görünümlere verileri bağlayabilirsiniz.
        Örneğin, `holder.recyclerRowBinding.recyclerViewTextView.setText(...)` gibi satırın içeriğini ayarlamak için kullanabilirsiniz.
        */

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {

        RecyclerRowBinding recyclerRowBinding;
        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}
