package com.example.katzen.Helper

import PacienteModel
import com.example.katzen.Config.Config
import com.example.katzen.Model.VentaMesDetalleModel

class ValidateFormulario {
    companion object{
        fun validarFormulario(vMDM : VentaMesDetalleModel) :  Pair<String, Boolean>  {
            var flag = true
            var message = ""

            if(!vMDM.kilometros.equals("")){
                val km =  vMDM.kilometros.toFloat()
                if(km < 0){
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
               /* if(vMDM.isEdit){
                    //val (costo,ganancia,venta) = GasHelper.calcularKm(vMDM.venta.toDouble(),vMDM.categoria)
                    vMDM.ganancia = vMDM.ganancia
                    vMDM.costo = vMDM.costo
                    vMDM.venta = vMDM.venta

                    if(Config.CATEGORIAS[4] != vMDM.categoria){
                        if(vMDM.venta.toDouble() < 50.00 ){
                            vMDM.venta = "50.00"
                        }
                    }

                }else{
                    val (costo,ganancia,venta) = GasHelper.calcularDomicilios(vMDM.kilometros.toDouble(),vMDM.categoria)
                    vMDM.ganancia = ganancia
                    vMDM.costo = costo
                    vMDM.venta = venta
                }*/

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
        fun validarFormulario(mM : PacienteModel) :  Pair<String, Boolean>  {
            var flag = true
            var message = ""

            if(mM.nombre.equals("")){
                flag = false
                message = "Debes escribir el nombre de la mascota."
            }
            if(mM.especie.equals("") || mM.especie.equals("Especie")){
                flag = false
                message = "Debes seleccionar una especie."
            }
            if(mM.sexo.equals("") || mM.sexo.equals("Sexo")){
                flag = false
                message = "Debes seleccionar un sexo."
            }
            if(mM.peso.equals("")){
                flag = false
                message = "Debes escribir el peso de la mascota."
            }else if(mM.peso.toDouble() < 0.00){
                flag = false
                message = "Debes ingresar un peso mayor a 0."
            }

            return Pair(message, flag)
        }
    }
}