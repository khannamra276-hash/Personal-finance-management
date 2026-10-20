package com.financeapp.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class UITheme {
    public static final Color PRIMARY = new Color(25, 118, 210);
    public static final Color PRIMARY_HOVER = new Color(21, 101, 192);
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color TEXT_MUTED = new Color(117, 117, 117);
    public static final Color BACKGROUND = new Color(248, 249, 250);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(224, 224, 224);

    public static final Color INCOME_COLOR = new Color(46, 125, 50);
    public static final Color EXPENSE_COLOR = new Color(198, 40, 40);
    public static final Color WARNING_COLOR = new Color(239, 108, 0);

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }
}

