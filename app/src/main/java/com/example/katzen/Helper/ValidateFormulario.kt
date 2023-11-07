package com.example.katzen.Helper

import com.example.katzen.Model.VentaMesDetalleModel

class ValidateFormulario {
    companion object{
        fun validarFormulario(vMDM : VentaMesDetalleModel) :  Pair<String, Boolean>  {
            var flag = true
            var message = ""

            if(!vMDM.kilometros.equals("")){
                val km =  vMDM.kilometros.toFloat()
                if(km < 1){
                    flag = false
                    message = "La cantidad de kilometros debe ser mayor a 0."
                }
            }else{
                flag = false
                message = "Debes escribir la cantidad de kilometros."
            }

            if(vMDM.categoria.equals("")){
                flag = false
                message = "Debes seleccionar una categoria."
            }else{
                if(vMDM.isEdit){
                    val (costo,ganancia,venta) = GasHelper.calcularKm(vMDM.venta.toDouble(),vMDM.categoria)
                    vMDM.ganancia = ganancia
                    vMDM.costo = costo
                    vMDM.venta = venta
                }else{
                    val (costo,ganancia,venta) = GasHelper.calcular(vMDM.kilometros.toDouble(),vMDM.categoria)
                    vMDM.ganancia = ganancia
                    vMDM.costo = costo
                    vMDM.venta = venta
                }

            }



            if (vMDM.fecha.equals("")){
                flag = false
                message = "Debes seleccionar una fecha."
            }else if(vMDM.venta.equals("")
                || vMDM.costo.equals("")
                || vMDM.ganancia.equals("")){
                flag = false
                // message = "Hubo un problema en el proceso intenta grabando el domicilio de nuevo."
            }

            return Pair(message, flag)
        }
    }
}