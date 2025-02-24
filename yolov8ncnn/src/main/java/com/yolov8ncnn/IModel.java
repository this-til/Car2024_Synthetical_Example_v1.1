package com.yolov8ncnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;

public interface IModel<I extends IItem> {

    Model<IItem.VehicleType> VEHICLE_MODEL = new Model<>("Vehicle", IItem.VehicleType.values().length, id -> IItem.VehicleType.values()[id]);
    Model<IItem.CardType> CARD_MODEL = new Model<>("Card", IItem.CardType.values().length, id -> IItem.CardType.values()[id]);
    Model<IItem.TrafficSignType> TRAFFIC_SIGN_MODEL = new Model<>("TrafficSign", IItem.TrafficSignType.values().length, id -> IItem.TrafficSignType.values()[id]);
    Model<IItem.TrafficLightType> TRAFFIC_LIGHT_MODEL = new Model<>("TrafficLight", IItem.TrafficLightType.values().length, id -> IItem.TrafficLightType.values()[id]);
    Model<IItem.ShapeAndColorType> SHAPE_AND_COLOR_MODEL = new Model<>("ShapeAndColor", IItem.ShapeAndColorType.values().length, id -> IItem.ShapeAndColorType.values()[id]);
    Model<IItem.MaskType> MASK_MODEL = new Model<>("Mask", IItem.MaskType.values().length, id -> IItem.MaskType.values()[id]);

    String getModelName();

    I asItem(int id);

    int getItemSize();

    String getExtractBlobName();

    void loadModel(AssetManager mgr);

    IItem.ItemCell<I>[] detect(Bitmap bitmap);
    
    boolean isLoaded();


    @Getter
    class Model<I extends IItem> implements IModel<I> {

        private String modelName;
        private int itemSize;
        private Function<Integer, I> asItem;
        private String extractBlobName = "output0";
        private boolean useGpu;
        private boolean loaded;

        public Model(String modelName, int itemSize, Function<Integer, I> asItem) {
            this.modelName = modelName;
            this.itemSize = itemSize;
            this.asItem = asItem;
        }


        @Override
        public I asItem(int id) {
            return asItem.apply(id);
        }

        @Override
        public void loadModel(AssetManager mgr) {
            Yolov8Ncnn.loadModel(mgr, getModelName(), getItemSize(), getExtractBlobName(), isUseGpu());
        }

        @Override
        public IItem.ItemCell<I>[] detect(Bitmap bitmap) {
            if (!loaded) {
                throw new RuntimeException("model not loaded");
            }
            Yolov8Ncnn.Obj[] detect = Yolov8Ncnn.detect(bitmap, getModelName());
            //noinspection unchecked
            return Arrays.stream(detect)
                    .map(item -> new IItem.ItemCell<>(asItem.apply(item.label), item.x, item.y, item.w, item.h, item.prob))
                    .toArray(IItem.ItemCell[]::new);
        }
    }


}
