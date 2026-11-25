package com.aydsii.demo.CU_34.model;

/**
 * Modalidad con la que se realizó una compra (CU-03: "modalidad de compra (web o presencial)").
 * Está junto a Pedido porque es un atributo de esa entidad. Ambos casos de uso comparten el
 * mismo tipo persistido, sin duplicar la entidad ni el enum.
 */
public enum ModalidadCompra {
    WEB,
    PRESENCIAL
}
