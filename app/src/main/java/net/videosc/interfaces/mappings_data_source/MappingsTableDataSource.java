package net.videosc.interfaces.mappings_data_source;

public interface MappingsTableDataSource<TRowHeaderDataType, TColumnHeaderDataType, TItemDataType> {

    int getRowsCount();
    int getColumnsCount();

//    TFirstHeaderDataType getFirstHeaderData();
    TRowHeaderDataType getRowHeaderDataType(int index);
    TColumnHeaderDataType getColumnHeaderData(int index);
    TItemDataType getItemData(int rowIndex, int columnIndex);
}
