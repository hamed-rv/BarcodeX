package com.hamedrahimvand.barcodex.model

/**
 *
 *@author Hamed.Rahimvand
 *@since 4/19/20
 */
enum class FlashType(value: Int) {
    ON(1), OFF(2), TORCH(3), AUTO(4);

    companion object {
        val DEFAULT = OFF
    }
}                                                                                                                                                                                                                                                                                                                                                                                           