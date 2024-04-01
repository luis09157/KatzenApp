import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.katzen.Model.InventarioModel
import com.example.katzen.Model.ProductoModel
import com.example.katzen.R
import com.squareup.picasso.Picasso

class ProductosAdapter(context: Context, private val productList: List<ProductoModel>) :
    ArrayAdapter<ProductoModel>(context, R.layout.producto_item_view, productList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.producto_item_view, parent, false)
            holder = ViewHolder()
            holder.imageView = itemView.findViewById(R.id.imagen)
            holder.nombreTextView = itemView.findViewById(R.id.textViewNombre)
            holder.precioTextView = itemView.findViewById(R.id.textViewPrecio)
            holder.descripcionTextView = itemView.findViewById(R.id.textViewDescripcion)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        val producto = productList[position]

        holder.nombreTextView?.text = ""
        holder.precioTextView?.text = ""
        holder.descripcionTextView?.text = ""
        holder.imageView?.setImageResource(R.drawable.img_venta)

        // Asigna los valores del producto a las vistas correspondientes
        holder.nombreTextView?.text = producto.nombre
        holder.precioTextView?.text = "Precio de venta: $${producto.precioVenta}"
        holder.descripcionTextView?.text = producto.descripcion

        // Cargar la imagen del producto utilizando Picasso
        if (producto.rutaImagen.isNotEmpty()) {
            // Si hay una URL de imagen disponible, cargar desde la URL
            Picasso.get().load(producto.rutaImagen).into(holder.imageView)
        } else {
            // Si no hay una URL de imagen, cargar desde el recurso drawable
            holder.imageView?.setImageResource(R.drawable.no_disponible_rosa)
        }

        return itemView!!
    }

    private class ViewHolder {
        var imageView: ImageView? = null
        var nombreTextView: TextView? = null
        var precioTextView: TextView? = null
        var descripcionTextView: TextView? = null
    }
}
