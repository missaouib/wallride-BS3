const webpack = require('webpack');
const path = require('path');
//const ExtractTextPlugin = require('extract-text-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: "./src/app.js",
	output: {
		path: path.resolve(__dirname, "dist"),
		filename: 'resources/guest/bundle.js'
	},
	module: {
		rules: [
			{
				test: /\.css$/,
				use: [
					{
						loader: MiniCssExtractPlugin.loader,
						options: {
							publicPath: '',
						},
					},
					'css-loader',
				] 
			},
			{
				test: /\.(jpg|png|gif)$/,
				use: 'url-loader',
			},
			{
				test: /\.(ttf|otf|eot|svg|woff2?)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
				exclude: /node_modules/,
				loader: 'file-loader',
				options: {
					context: path.resolve(__dirname, 'src/resources'),
					name: '[path][name].[ext]',
					// publicPath: './font/',
					emitFile: false
				}
			}
		]
	},
	plugins: [
		new webpack.ProvidePlugin({
			$: "jquery",
			jQuery: "jquery",
			"window.jQuery": "jquery",
			"global.jQuery": "jquery"
		}),
		//new ExtractTextPlugin("resources/guest/bundle.css"),
		new MiniCssExtractPlugin({
			filename: 'resources/guest/bundle.css',
			chunkFilename: 'resources/guest/[name].css'
		}),
		new CopyWebpackPlugin({
			patterns:[
				//{ from: 'node_modules/bootstrap/dist/fonts/*', to: 'resources/guest' },
				{ context: 'src/resources', from: 'fonts/**/*', to: 'resources/guest' },
				{ context: 'src/resources', from: 'img/**/*', to: 'resources/guest' },
				{ context: 'src/templates', from: '**/*', to: 'templates/guest' }
			],
		})
	],
	devServer: {
		contentBase: [
			path.resolve(__dirname, "src/templates"),
			path.resolve(__dirname, "src/resources")
		],
		port: 8000
	}
};