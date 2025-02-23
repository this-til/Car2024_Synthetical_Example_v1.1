package com.yolov8ncnn;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

public interface IModel<I extends IItem> {

    Model<IItem.VehicleType> VEHICLE_MODEL = new Model<>("Vehicle", id -> IItem.VehicleType.values()[id]);
    Model<IItem.CardType> CARD_MODEL = new Model<>("Card", id -> IItem.CardType.values()[id]);
    Model<IItem.TrafficSignType> TRAFFIC_SIGN_MODEL = new Model<>("TrafficSign", id -> IItem.TrafficSignType.values()[id]);
    Model<IItem.TrafficLightType> TRAFFIC_LIGHT_MODEL = new Model<>("TrafficLight", id -> IItem.TrafficLightType.values()[id]);
    Model<IItem.ShapeAndColorType> SHAPE_AND_COLOR_MODEL = new Model<>("ShapeAndColor", id -> IItem.ShapeAndColorType.values()[id]);
    Model<IItem.MaskType> MASK_MODEL = new Model<>("Mask", id -> IItem.MaskType.values()[id]);
    
    String getModelName();

    I asItem(int id);

    @Getter
    @AllArgsConstructor
    class Model<I extends IItem> implements IModel<I> {

        private String modelName;

        private Function<Integer, I> asItem;


        @Override
        public I asItem(int id) {
            return asItem.apply(id);
        }
    }

}
