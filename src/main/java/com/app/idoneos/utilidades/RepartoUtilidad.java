package com.app.idoneos.utilidades;

import java.util.List;

/**
 * Reparto de una cantidad entera entre varias partes, en forma proporcional y sin perder nada por el redondeo.
 */
public final class RepartoUtilidad {

    private RepartoUtilidad() {
    }

    /**
     * Reparte un total entre partes en proporción a sus pesos actuales. Cada parte recibe al menos 1 y la suma de lo
     * repartido es siempre exactamente el total: lo que sobra del redondeo va a las partes con mayor resto.
     *
     * @param total El total a repartir; debe ser al menos la cantidad de partes.
     * @param pesos El peso de cada parte (un valor menor a 1 cuenta como 1).
     * @return Lo que recibe cada parte, en el mismo orden que los pesos.
     * @throws IllegalArgumentException si el total no alcanza para darle 1 a cada parte.
     */
    public static int[] repartirProporcional(int total, List<Integer> pesos) {
        int partes = pesos.size();
        if (total < partes) {
            throw new IllegalArgumentException("El total (" + total + ") no alcanza para repartir entre " + partes + " partes");
        }
        int[] resultado = new int[partes];
        if (partes == 0) {
            return resultado;
        }
        long pesoTotal = 0;
        for (int peso : pesos) {
            pesoTotal += Math.max(1, peso);
        }
        // Cada parte recibe 1 y el resto se reparte en proporción a su peso
        long sobrante = (long) total - partes;
        long[] restos = new long[partes];
        long asignado = 0;
        for (int i = 0; i < partes; i++) {
            long producto = sobrante * Math.max(1, pesos.get(i));
            resultado[i] = 1 + (int) (producto / pesoTotal);
            restos[i] = producto % pesoTotal;
            asignado += resultado[i] - 1;
        }
        // Lo que quedó sin asignar por el redondeo, de a uno, a las partes con mayor resto (a igual resto, la primera)
        for (long pendiente = sobrante - asignado; pendiente > 0; pendiente--) {
            int mayor = 0;
            for (int i = 1; i < partes; i++) {
                if (restos[i] > restos[mayor]) {
                    mayor = i;
                }
            }
            resultado[mayor]++;
            restos[mayor] = -1;
        }
        return resultado;
    }
}
